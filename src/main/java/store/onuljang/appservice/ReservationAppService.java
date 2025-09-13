package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.request.ReservationRequest;
import store.onuljang.controller.response.ReservationListResponse;
import store.onuljang.exception.UserValidateException;
import store.onuljang.log.user_product.UserReservationLogEvent;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.ProductRestockTarget;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.repository.entity.enums.UserProductAction;
import store.onuljang.service.ProductsService;
import store.onuljang.service.ReservationService;
import store.onuljang.service.UserService;
import store.onuljang.util.TimeUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static store.onuljang.util.TimeUtil.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationAppService {
    ReservationService reservationService;
    UserService userService;
    ProductsService productsService;
    ApplicationEventPublisher eventPublisher;

    @Transactional
    public long reserve(String uId, ReservationRequest request) {
        Product product = productsService.findByIdWithLock(request.productId());
        validateReserveTime(product.getSellDate(), product.getSellTime());

        Users user = userService.findByUidWithLock(uId);
        Reservation reservation = Reservation.builder()
            .user(user)
            .product(product)
            .quantity(request.quantity())
            .amount(product.getPrice().multiply(BigDecimal.valueOf(request.quantity())))
            .pickupDate(product.getSellDate())
            .build();

        product.reserve(request.quantity());
        reservationService.save(reservation);
        user.reserve(request.quantity(), TimeUtil.nowDate());

        saveReservationLog(user.getUid(), reservation.getId(), UserProductAction.CREATE);

        return reservation.getId();
    }

    @Transactional
    public void cancel(String uId, long reservationId) {
        Reservation reservation = reservationService.findByIdWithLock(reservationId);
        Product product = productsService.findByIdWithLock(reservation.getProduct().getId());
        Users user = userService.findByUidWithLock(uId);

        validateUserReservation(user, reservation);

        reservation.cancelByUser(TimeUtil.nowDate(), RESERVE_DEADLINE, KST);
        product.cancel(reservation.getQuantity());
        user.cancelReservation(reservation.getQuantity());

        saveReservationLog(user.getUid(), reservation.getId(), UserProductAction.DELETE);
    }

    @Transactional
    public void selfPick(String uId, long reservationId) {
        Users user = userService.findByUidWithLock(uId);
        Reservation reservation = reservationService.findByIdWithLock(reservationId);

        validateUserReservation(user, reservation);
        user.assertCanSelfPick();
        productsService.findById(reservation.getProduct().getId());

        reservation.requestSelfPick(TimeUtil.nowDate(), RESERVE_DEADLINE, KST);

        saveReservationLog(user.getUid(), reservation.getId(), UserProductAction.UPDATE);
    }

    @Transactional
    public int cancelNoShow(LocalDate today, ReservationStatus before, ReservationStatus after, LocalDateTime now) {
        Set<Long> targetIds = reservationService.findIdsByPickupDateAndStatus(today, before);
        if (targetIds.isEmpty()) {
            return 0;
        }

        int updateReservationRows = reservationService.updateAllReservationsWhereIdIn(targetIds, today, before, after, now);
        if (updateReservationRows != targetIds.size()) {
            throw new IllegalStateException("동시에 변경된 예약이 있어 취소/재고복원이 일치하지 않습니다.");
        }

        List<ProductRestockTarget> noShowReservations =
                reservationService.findAllByIdInAndStatusGroupByProductIdOrderByProductId(targetIds, after);

        for (ProductRestockTarget restockTarget : noShowReservations) {
            Product product = productsService.findByIdWithLock(restockTarget.getProductId());

            product.addStock(restockTarget.getQuantity());
        }

        return updateReservationRows;
    }

    @Transactional(readOnly = true)
    public ReservationListResponse getReservations(String uId, LocalDate from, LocalDate to) {
        Users user = userService.findByUId(uId);

        List<Reservation> entities = reservationService.findAllByUserAndOrderDateBetweenWithProductOrderByOrderDate(user, from, to);

        return ReservationListResponse.from(entities);
    }

    private void validateUserReservation(Users user, Reservation reservation) {
        if (!user.getUid().equals(reservation.getUser().getUid())) {
            throw new UserValidateException("다른 유저가 예약한 상품입니다.");
        }
    }

    private void saveReservationLog(String uid, Long reservationId, UserProductAction action) {
        eventPublisher.publishEvent(
            UserReservationLogEvent.builder()
                .userUid(uid)
                .reservationId(reservationId)
                .action(action)
                .build());
    }

    private void validateReserveTime(LocalDate sellDate, LocalTime sellTime) {
        ZonedDateTime deadLine = sellDate.atTime(RESERVE_DEADLINE).atZone(KST);
        if (ZonedDateTime.now(KST).isAfter(deadLine)) {
            throw new UserValidateException("예약 가능한 시간이 지났습니다.");
        }

        LocalDateTime postDateTime = sellDate.atTime(sellTime);
        if (TimeUtil.nowDateTime().isBefore(postDateTime)) {
            throw new UserValidateException("상품 게시 시간 전입니다.");
        }
    }
}
