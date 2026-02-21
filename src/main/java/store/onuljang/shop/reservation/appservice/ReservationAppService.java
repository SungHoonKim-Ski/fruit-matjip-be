package store.onuljang.shop.reservation.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shop.reservation.dto.ReservationRequest;
import store.onuljang.shop.reservation.dto.ReservationListResponse;
import store.onuljang.shared.exception.UserValidateException;
import store.onuljang.shop.product.event.UserReservationLogEvent;
import store.onuljang.shop.product.entity.Product;
import store.onuljang.shop.reservation.entity.Reservation;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.entity.enums.DeliveryStatus;
import store.onuljang.shared.entity.enums.UserProductAction;
import store.onuljang.shop.product.service.ProductsService;
import store.onuljang.shop.reservation.service.ReservationService;
import store.onuljang.shared.user.service.UserService;
import store.onuljang.shop.delivery.service.DeliveryOrderService;
import store.onuljang.shared.util.DisplayCodeGenerator;
import store.onuljang.shared.util.TimeUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

import static store.onuljang.shared.util.TimeUtil.*;

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
    public String reserve(String uId, ReservationRequest request) {
        Product product = productsService.findByIdWithLock(request.productId());
        validateReserveTime(product.getSellDate(), product.getSellTime());

        Users user = userService.findByUidWithLock(uId);
        validateNotRestricted(user);
        Reservation reservation = Reservation.builder()
            .user(user)
            .product(product)
            .quantity(request.quantity())
            .amount(product.getPrice().multiply(BigDecimal.valueOf(request.quantity())))
            .pickupDate(product.getSellDate())
            .sellPrice(product.getPrice())
            .build();

        reservation.setDisplayCode(
                DisplayCodeGenerator.generateUnique("R", TimeUtil.nowDateTime(), reservationService::existsByDisplayCode));

        product.reserve(request.quantity());
        reservationService.save(reservation);
        user.reserve(request.quantity(), reservation.getAmount(), TimeUtil.nowDate());

        saveReservationLog(user.getUid(), reservation.getId(), UserProductAction.CREATE);

        return reservation.getDisplayCode();
    }

    @Transactional
    public void minusQuantity(String uId, String displayCode, int minusQuantity) {
        Reservation reservation = reservationService
                .findByDisplayCodeWithLock(DisplayCodeGenerator.resolveCode("R", displayCode));
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
    public void cancel(String uId, String displayCode) {
        Reservation reservation = reservationService
                .findByDisplayCodeWithLock(DisplayCodeGenerator.resolveCode("R", displayCode));
        Product product = productsService.findByIdWithLock(reservation.getProduct().getId());
        Users user = userService.findByUidWithLock(uId);

        validateUserReservation(user, reservation);
        deliveryOrderService.findByReservation(reservation).ifPresent(order -> {
            if (order.getStatus() == DeliveryStatus.PAID
                || order.getStatus() == DeliveryStatus.OUT_FOR_DELIVERY
                || order.getStatus() == DeliveryStatus.DELIVERED) {
                throw new UserValidateException("결제 완료된 배달 예약은 취소할 수 없습니다.");
            }
        });

        reservation.cancelByUser();
        product.cancel(reservation.getQuantity());
        user.cancelReserve(reservation.getQuantity(), reservation.getAmount());

        saveReservationLog(user.getUid(), reservation.getId(), UserProductAction.DELETE);
    }

    @Transactional(readOnly = true)
    public ReservationListResponse getReservations(String uId, LocalDate from, LocalDate to) {
        Users user = userService.findByUId(uId);

        List<Reservation> entities = reservationService
                .findAllByUserAndPickupDateBetweenWithProductAllAndDeliveryOrderByPickupDateDesc(user, from, to);
        return ReservationListResponse.from(entities);
    }

    private void validateNotRestricted(Users user) {
        if (user.isRestricted()) {
            LocalDate until = user.getRestrictedUntil().plusDays(1);
            throw new UserValidateException(
                    String.format("노쇼로 인해 서비스 이용이 제한됩니다. %d/%02d/%02d 이후부터 이용 가능합니다.",
                            until.getYear(), until.getMonthValue(), until.getDayOfMonth()));
        }
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
