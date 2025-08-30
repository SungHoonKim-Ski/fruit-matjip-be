package store.onuljang.appservice;

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
import store.onuljang.util.TimeUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminReservationAppService {
    ReservationService reservationService;
    UserService userService;

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
    public int bulkUpdateReservationsStatus(AdminUpdateReservationsRequest request) {
        List<Reservation> reservationList = reservationService.findAllUserIdInWithUserWithLock(request.reservationIds());
        if (reservationList.isEmpty()) {
            return 0;
        }

        validateBulkReservationsUpdate(reservationList, request.status());

        return reservationService.bulkUpdateReservationsStatus(
            request.reservationIds(), request.status(), LocalDateTime.now(TimeUtil.KST)
        );
    }

    @Transactional(readOnly = true)
    public AdminReservationReportResponse getSails(LocalDate from, LocalDate to) {
        List<ReservationAll> entities = reservationService.findAllByStatusInAndPickupDateBetweenIncludingDeleted(
            List.of(ReservationStatus.PICKED, ReservationStatus.SELF_PICK, ReservationStatus.SELF_PICK_READY)
            ,from, to);

        return AdminReservationReportResponse.from(entities);
    }

    @Transactional(readOnly = true)
    public AdminReservationListResponse getAllByDate(LocalDate date) {
        List<Reservation> entities = reservationService.finAllByDateWithUserAndProduct(date);

        return AdminReservationListResponse.from(entities);
    }

    private void validateBulkReservationsUpdate(List<Reservation> reservationSet, ReservationStatus beforeStatus) {
        if (reservationSet == null || reservationSet.isEmpty()) {
            throw new IllegalArgumentException("예약이 없습니다.");
        }

        if (beforeStatus == ReservationStatus.CANCELED) {
            throw new IllegalStateException("예약 취소는 한번에 변경이 불가능합니다.");
        }

        Reservation first = reservationSet.iterator().next();

        ReservationStatus currentStatue = first.getStatus();
        boolean allSameStatus = reservationSet.stream().allMatch(r -> r.getStatus() == currentStatue);
        if (!allSameStatus) {
            throw new IllegalArgumentException("동일한 상태의 예약만 한번에 변경할 수 있습니다.");
        }

        if (currentStatue == beforeStatus) {
            throw new IllegalArgumentException("변경하려는 예약의 상태가 동일합니다.");
        }

        String uid = first.getUser().getUid();
        boolean allSameUser = reservationSet.stream().allMatch(r -> r.getUser().getUid().equals(uid));
        if (!allSameUser) {
            throw new IllegalStateException("동일한 유저의 예약만 한번에 변경할 수 있습니다.");
        }
    }
}
