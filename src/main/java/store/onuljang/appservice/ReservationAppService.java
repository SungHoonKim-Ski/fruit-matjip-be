package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.request.ReservationListRequest;
import store.onuljang.controller.request.ReservationRequest;
import store.onuljang.controller.response.ReservationListResponse;
import store.onuljang.exception.ProductExceedException;
import store.onuljang.exception.UserValidateException;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;
import store.onuljang.service.ProductsService;
import store.onuljang.service.ReservationService;
import store.onuljang.service.UserService;

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
        Product product = productsService.findByIdWithLock(request.productId());
        if (product.getStock() < request.quantity()) {
            throw new ProductExceedException("상품의 재고가 부족합니다.");
        }

        product.removeStock(request.quantity());
        return reservationService.save(uId, request.productId(), request.quantity(), request.amount());
    }

    @Transactional
    public void cancel(String uId, long reservationId) {
        Users user = userService.findByUId(uId);
        Reservation reservation = reservationService.findById(reservationId);

        validateReservation(user, reservation);

        Product product = productsService.findByIdWithLock(reservation.getProduct().getId());
        product.addStock(reservation.getQuantity());

        reservationService.cancel(reservation);
    }

    @Transactional(readOnly = true)
    public ReservationListResponse getReservations(String uId, ReservationListRequest request) {
        Users user = userService.findByUId(uId);

        List<Reservation> entities = reservationService.findAllByUserAndOrderDateBetweenWithProduct(user, request.from(), request.to());

        return ReservationListResponse.from(entities);
    }

    public void validateReservation(Users user, Reservation reservation) {
        if (!user.getUid().equals(reservation.getUser().getUid())) {
            throw new UserValidateException("다른 유저가 예약한 상품입니다.");
        }
    }
}
