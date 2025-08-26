package store.onuljang.appservice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.request.AdminUpdateReservationsRequest;
import store.onuljang.controller.response.AdminReservationListResponse;
import store.onuljang.controller.response.AdminReservationReportResponse;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.ReservationAll;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.service.ReservationService;
import store.onuljang.service.UserService;

import java.time.LocalDate;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminReservationAppService {
    ReservationService reservationService;
    UserService userService;

    @Transactional(readOnly = true)
    public AdminReservationListResponse getAllByDate(LocalDate date) {
        List<Reservation> entities = reservationService.finAllByDateWithUserAndProduct(date);

        return AdminReservationListResponse.from(entities);
    }

    @Transactional
    public void updateReservationStatus(long id, ReservationStatus status) {
        Reservation entity = reservationService.findByIdWithLock(id);

        entity.setStatus(status);
    }

    @Transactional
    public void warnReservationUser(long id) {
        Reservation reservation = reservationService.findById(id);

        Users user = userService.findByUidWithLock(reservation.getUser().getUid());

        user.warn();
    }

    @Transactional
    public int updateReservationsStatus(AdminUpdateReservationsRequest request) {
        return reservationService.updateReservationsStatus(request.reservationIds(), request.status());
    }

    @Transactional(readOnly = true)
    public AdminReservationReportResponse getSails(LocalDate from, LocalDate to) {
        List<ReservationAll> entities = reservationService.findAllByStatusInAndPickupDateBetweenIncludingDeleted(
                List.of(ReservationStatus.PICKED, ReservationStatus.SELF_PICK, ReservationStatus.SELF_PICK_READY)
                ,from, to);

        return AdminReservationReportResponse.from(entities);
    }
}
