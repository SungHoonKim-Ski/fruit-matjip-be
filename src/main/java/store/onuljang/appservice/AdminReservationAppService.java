package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.request.AdminUpdateReservationsRequest;
import store.onuljang.controller.response.AdminReservationListResponse;
import store.onuljang.controller.response.AdminReservationsTodayResponse;
import store.onuljang.exception.UserValidateException;
import store.onuljang.repository.entity.*;
import store.onuljang.repository.entity.enums.AggPhase;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.service.*;
import store.onuljang.util.TimeUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class AdminReservationAppService {
    ReservationService reservationService;
    UserService userService;
    ProductsService productService;
    AggAppliedService aggAppliedService;

    @Transactional
    public void updateReservationStatus(long id, ReservationStatus status) {
        Reservation reservation = reservationService.findByIdWithLock(id);

        reservation.setStatus(status);
    }

    @Transactional
    public void handleNoShow(long id) {
        Reservation reservation = reservationService.findByIdWithLock(id);
        Product product = productService.findByIdWithLock(reservation.getProduct().getId());
        Users user = userService.findByUidWithLock(reservation.getUser().getUid());

        validateUserReservation(user, reservation);

        if (reservation.isNoShow()) {
            user.warn();
        } else {
            reservation.noShow();
            product.cancel(reservation.getQuantity());
            user.noShow(reservation.getQuantity(), reservation.getAmount());
        }

        aggAppliedService.markSingle(reservation.getId(), AggPhase.NO_SHOW_MINUS);
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
    public AdminReservationsTodayResponse getTodaySales() {
        List<ReservationSalesRow> salesRows = reservationService.findPickupDateSales(
            Set.of(ReservationStatus.PENDING, ReservationStatus.PICKED, ReservationStatus.SELF_PICK, ReservationStatus.SELF_PICK_READY),
            TimeUtil.nowDate()
        );

        return AdminReservationsTodayResponse.from(salesRows);
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

    private void validateUserReservation(Users user, Reservation reservation) {
        if (!user.getUid().equals(reservation.getUser().getUid())) {
            throw new UserValidateException("다른 유저가 예약한 상품입니다.");
        }
    }
}
