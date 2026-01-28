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
import store.onuljang.event.user_product.UserReservationLogEvent;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.UserProductAction;
import store.onuljang.service.ProductsService;
import store.onuljang.service.ReservationService;
import store.onuljang.service.UserService;
import store.onuljang.service.DeliveryOrderService;
import store.onuljang.util.TimeUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

import static store.onuljang.util.TimeUtil.*;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationAppService {
    ReservationService reservationService;
    UserService userService;
    ProductsService productsService;
    DeliveryOrderService deliveryOrderService;
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
            .sellPrice(product.getPrice())
            .build();

        product.reserve(request.quantity());
        reservationService.save(reservation);
        user.reserve(request.quantity(), reservation.getAmount(), TimeUtil.nowDate());

        saveReservationLog(user.getUid(), reservation.getId(), UserProductAction.CREATE);

        return reservation.getId();
    }

    @Transactional
    public void minusQuantity(String uId, long reservationId, int minusQuantity) {
        Reservation reservation = reservationService.findByIdWithLock(reservationId);
        Product product = productsService.findByIdWithLock(reservation.getProduct().getId());
        Users user = userService.findByUidWithLock(uId);

        validateUserReservation(user, reservation);

        reservation.minusQuantityByUser(minusQuantity);
        product.addStock(minusQuantity);

        BigDecimal diff = reservation.getSellPrice().multiply(BigDecimal.valueOf(minusQuantity));
        user.cancelReserve(minusQuantity, diff);

        saveReservationLog(user.getUid(), reservation.getId(), UserProductAction.UPDATE);
    }

    @Transactional
    public void cancel(String uId, long reservationId) {
        Reservation reservation = reservationService.findByIdWithLock(reservationId);
        Product product = productsService.findByIdWithLock(reservation.getProduct().getId());
        Users user = userService.findByUidWithLock(uId);

        validateUserReservation(user, reservation);
        deliveryOrderService.findByReservation(reservation).ifPresent(order -> {
            if (order.getStatus() == store.onuljang.repository.entity.enums.DeliveryStatus.PAID
                || order.getStatus() == store.onuljang.repository.entity.enums.DeliveryStatus.OUT_FOR_DELIVERY
                || order.getStatus() == store.onuljang.repository.entity.enums.DeliveryStatus.DELIVERED) {
                throw new UserValidateException("결제 완료된 배달 예약은 취소할 수 없습니다.");
            }
        });

        reservation.cancelByUser();
        product.cancel(reservation.getQuantity());
        user.cancelReserve(reservation.getQuantity(), reservation.getAmount());

        saveReservationLog(user.getUid(), reservation.getId(), UserProductAction.DELETE);
    }

    @Transactional
    @Deprecated
    public void selfPick(String uId, long reservationId) {
        Reservation reservation = reservationService.findByIdWithLock(reservationId);
        Product product = productsService.findById(reservation.getProduct().getId());
        Users user = userService.findByUidWithLock(uId);

        validateUserReservation(user, reservation);
        user.assertCanSelfPick();
        product.assertCanSelfPick();

        reservation.requestSelfPick(TimeUtil.nowDate(), SELF_PICK_DEADLINE, KST);

        saveReservationLog(user.getUid(), reservation.getId(), UserProductAction.UPDATE);
    }

    @Transactional(readOnly = true)
    public ReservationListResponse getReservations(String uId, LocalDate from, LocalDate to) {
        Users user = userService.findByUId(uId);

        List<Reservation> entities = reservationService
                .findAllByUserAndPickupDateBetweenWithProductAllAndDeliveryOrderByPickupDateDesc(user, from, to);
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
        if (TimeUtil.nowZonedDateTime().isAfter(deadLine)) {
            throw new UserValidateException("예약 가능한 시간이 지났습니다.");
        }

        if (sellTime != null) {
            LocalDateTime postDateTime = sellDate.atTime(sellTime);
            if (TimeUtil.nowDateTime().isBefore(postDateTime)) {
                throw new UserValidateException("상품 게시 시간 전입니다.");
            }
        }
    }
}
