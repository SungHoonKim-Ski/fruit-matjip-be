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
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationAppService {
    ReservationService reservationService;
    UserService userService;
    ProductsService productsService;

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
            .orderDate(product.getSellDate())
            .build();

        return reservationService.save(entity);
    }

    @Transactional
    public void cancel(String uId, long reservationId) {
        Users user = userService.findByuIdWithLock(uId);
        Reservation reservation = reservationService.findByIdWithLock(reservationId);

        validateReservation(user, reservation);

        Product product = productsService.findByIdWithLock(reservation.getProduct().getId());
        product.addStock(reservation.getQuantity());

        reservationService.cancel(reservation);

        user.removeTotalOrders(reservation.getQuantity());
    }

    @Transactional(readOnly = true)
    public ReservationListResponse getReservations(String uId, LocalDate from, LocalDate to) {
        Users user = userService.findByUId(uId);

        List<Reservation> entities = reservationService.findAllByUserAndOrderDateBetweenWithProductOrderByOrderDate(user, from, to);

        return ReservationListResponse.from(entities);
    }

    public void validateReservation(Users user, Reservation reservation) {
        if (!user.getUid().equals(reservation.getUser().getUid())) {
            throw new UserValidateException("다른 유저가 예약한 상품입니다.");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new UserValidateException("취소할 수 없는 예약입니다.");
        }
    }
}
