package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.request.ReservationRequest;
import store.onuljang.controller.response.ReservationListResponse;
import store.onuljang.exception.UserValidateException;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.service.ProductsService;
import store.onuljang.service.ReservationService;
import store.onuljang.service.UserService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationAppService {
    ReservationService reservationService;
    UserService userService;
    ProductsService productsService;

    ZoneId KST = ZoneId.of("Asia/Seoul");
    LocalTime SELF_PICK_DEADLINE = LocalTime.of(18, 30);
    ZonedDateTime nowKst = ZonedDateTime.now(KST);

    @Transactional
    public long reserve(String uId, ReservationRequest request) {
        Users user = userService.findByuIdWithLock(uId);
        Product product = productsService.findByIdWithLock(request.productId());

        product.reserve(request.quantity());
        user.reserve(request.quantity());

        Reservation entity = Reservation.builder()
            .user(user)
            .product(product)
            .quantity(request.quantity())
            .amount(request.amount())
            .pickupDate(product.getSellDate())
            .build();

        return reservationService.save(entity);
    }

    @Transactional
    public void cancel(String uId, long reservationId) {
        Users user = userService.findByuIdWithLock(uId);
        Reservation reservation = reservationService.findByIdWithLock(reservationId);

        if (reservation.getStatus() == ReservationStatus.PENDING) {
            validateCancelReservation(user, reservation);
        } else if (reservation.getStatus() == ReservationStatus.SELF_PICK) {
            validateSelfPickCancelReservation(user, reservation);
        }

        Product product = productsService.findByIdWithLock(reservation.getProduct().getId());
        product.addStock(reservation.getQuantity());

        user.cancelReservation(reservation.getQuantity(), reservation.getStatus());

        reservationService.cancel(reservation);
    }

    @Transactional
    public void selfPick(String uId, long reservationId) {
        Users user = userService.findByUId(uId);
        Reservation reservation = reservationService.findByIdWithLock(reservationId);

        validateSelfPickReservation(user, reservation);

        productsService.findById(reservation.getProduct().getId());

        reservationService.selfPick(reservation);
    }

    @Transactional(readOnly = true)
    public ReservationListResponse getReservations(String uId, LocalDate from, LocalDate to) {
        Users user = userService.findByUId(uId);

        List<Reservation> entities = reservationService.findAllByUserAndOrderDateBetweenWithProductOrderByOrderDate(user, from, to);

        return ReservationListResponse.from(entities);
    }

    private void validateSelfPickReservation(Users user, Reservation reservation) {
        if (!user.getUid().equals(reservation.getUser().getUid())) {
            throw new UserValidateException("다른 유저가 예약한 상품입니다.");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new UserValidateException("셀프 수령으로 변경할 수 없는 예약입니다.");
        }

        if (user.exceedMaxWarnCount()) {
            throw new UserValidateException("이번달에는 더이상 셀프 수령 신청을 할 수 없습니다.");
        }

        ZonedDateTime deadline = reservation.getPickupDate().atTime(SELF_PICK_DEADLINE).atZone(KST);
        if (nowKst.isAfter(deadline)) {
            throw new UserValidateException("셀프 수령 신청이 마감됬습니다.");
        }
    }

    private void validateCancelReservation(Users user, Reservation reservation) {
        if (!user.getUid().equals(reservation.getUser().getUid())) {
            throw new UserValidateException("다른 유저가 예약한 상품입니다.");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new UserValidateException("취소할 수 없는 예약입니다.");
        }
        if (reservation.getPickupDate().isBefore(LocalDate.now(KST))) {
            throw new UserValidateException("과거 예약은 취소할 수 없습니다.");
        }
    }

    private void validateSelfPickCancelReservation(Users user, Reservation reservation) {
        if (!user.getUid().equals(reservation.getUser().getUid())) {
            throw new UserValidateException("다른 유저가 예약한 상품입니다.");
        }
        if (reservation.getStatus() != ReservationStatus.SELF_PICK) {
            throw new UserValidateException("취소할 수 없는 예약입니다.");
        }
        if (user.exceedMaxWarnCount()) {
            throw new UserValidateException("이번달 셀프 수령 취소 가능 횟수를 초과했습니다.");
        }
        if (reservation.getPickupDate().isBefore(LocalDate.now(KST))) {
            throw new UserValidateException("과거 예약은 취소할 수 없습니다.");
        }
    }
}
