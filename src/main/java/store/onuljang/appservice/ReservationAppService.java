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
import store.onuljang.service.ProductsService;
import store.onuljang.service.ReservationService;
import store.onuljang.service.UserService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationAppService {
    ReservationService reservationService;
    UserService userService;
    ProductsService productsService;

    static ZoneId KST = ZoneId.of("Asia/Seoul");
    static LocalTime SELF_PICK_DEADLINE = LocalTime.of(18, 0);

    @Transactional
    public long reserve(String uId, ReservationRequest request) {
        Users user = userService.findByUidWithLock(uId);
        Product product = productsService.findByIdWithLock(request.productId());

        product.reserve(request.quantity());

        user.reserve(request.quantity(), LocalDate.now(KST));

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
        Users user = userService.findByUidWithLock(uId);
        Reservation reservation = reservationService.findByIdWithLock(reservationId);

        validateUserReservation(user, reservation);

        user.cancelReservation(reservation.getQuantity());

        Product product = productsService.findByIdWithLock(reservation.getProduct().getId());
        product.cancel(reservation.getQuantity());

        reservation.cancelByUser(LocalDate.now(KST), SELF_PICK_DEADLINE, KST);
    }

    @Transactional
    public void selfPick(String uId, long reservationId) {
        Users user = userService.findByUidWithLock(uId);
        Reservation reservation = reservationService.findByIdWithLock(reservationId);

        validateUserReservation(user, reservation);
        user.assertCanSelfPick();
        productsService.findById(reservation.getProduct().getId());

        reservation.requestSelfPick(LocalDate.now(KST), SELF_PICK_DEADLINE, KST);
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
}
