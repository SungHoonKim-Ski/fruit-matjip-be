package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.request.ReservationListRequest;
import store.onuljang.controller.request.ReservationRequest;
import store.onuljang.controller.response.ReservationListResponse;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;
import store.onuljang.service.ReservationService;
import store.onuljang.service.UserService;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationAppService {
    ReservationService reservationService;
    UserService userService;

    @Transactional
    public long save(String uId, ReservationRequest request) {
        return reservationService.save(uId, request.productId(), request.quantity(), request.amount());
    }

    @Transactional
    public void cancel(String uId, long reservationId) {
        Users user = userService.findByUId(uId);
        Reservation reservation = reservationService.findById(reservationId);

        validateReservation(user, reservation);

        reservationService.cancel(reservation);
    }

    @Transactional(readOnly = true)
    public ReservationListResponse getReservations(String uId, ReservationListRequest request) {
        Users user = userService.findByUId(uId);

        List<Reservation> entities = reservationService.findAllByUserAndOrderDateBetweenWithProduct(user, request.from(), request.to());

        return ReservationListResponse.from(entities);
    }

    public void validateReservation(Users user, Reservation reservation) {
        if (!user.getInternalUid().equals(reservation.getUser().getInternalUid())) {
            throw new IllegalArgumentException("다른 유저가 예약한 상품");
        }
    }
}
