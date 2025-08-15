package store.onuljang.appservice;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.response.AdminReservationListResponse;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.service.ReservationService;

import java.time.LocalDate;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminReservationAppService {
    ReservationService reservationService;

    @Transactional(readOnly = true)
    public AdminReservationListResponse getAllByDate(LocalDate date) {
        List<Reservation> entities = reservationService.finAllByDateWithUserAndProduct(date);

        return AdminReservationListResponse.from(entities);
    }

    @Transactional
    public void togglePicked(long id) {
        Reservation entity = reservationService.findById(id);

        entity.togglePicked();
    }

    @Transactional(readOnly = true)
    public AdminReservationListResponse getReports(LocalDate from, LocalDate to) {
        List<Reservation> entities = reservationService.findAllByStatusAndOrderDateBetween(from, to);

        return AdminReservationListResponse.from(entities);
    }
}
