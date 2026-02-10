package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.request.AdminUpdateReservationsRequest;
import store.onuljang.controller.response.AdminReservationListResponse;
import store.onuljang.controller.response.AdminReservationsTodayResponse;
import store.onuljang.exception.AlreadyNoShowAdminException;
import store.onuljang.exception.UserValidateException;
import store.onuljang.event.user_message.UserMessageEvent;
import store.onuljang.repository.entity.*;
import store.onuljang.repository.entity.enums.AggPhase;
import store.onuljang.repository.entity.enums.MessageType;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.service.*;
import store.onuljang.util.TimeUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class AdminReservationAppService {
    ReservationService reservationService;
    UserService userService;
    ProductsService productService;
    AggAppliedService aggAppliedService;
    UserWarnService userWarnService;
    ApplicationEventPublisher eventPublisher;

    @Transactional
    public void updateReservationStatus(long id, ReservationStatus status) {
        Reservation reservation = reservationService.findByIdWithLock(id);
        if (status == ReservationStatus.NO_SHOW) {
            throw new IllegalArgumentException("노쇼 경고로만 노쇼 상태로 변경할 수 있습니다.");
        }
        if (reservation.isNoShow()) {
            throw new IllegalArgumentException("노쇼 예약은 변경할 수 없습니다.");
        }
        reservation.setStatus(status);
    }

    @Transactional
    public void handleNoShow(long id) {
        Reservation reservation = reservationService.findByIdWithLock(id);
        if (reservation.isNoShow()) {
            throw new AlreadyNoShowAdminException("이미 노쇼 처리된 예약입니다;");
        }

        Product product = productService.findByIdWithLock(reservation.getProduct().getId());
        Users user = userService.findByUidWithLock(reservation.getUser().getUid());

        validateUserReservation(user, reservation);

        reservation.noShow();
        product.cancel(reservation.getQuantity());
        user.noShow(reservation.getQuantity(), reservation.getAmount());

        userWarnService.noShow(user);

        publishUserNoShowMessage(user.getUid());
        aggAppliedService.markSingle(reservation.getId(), AggPhase.NO_SHOW_MINUS);
    }

    @Transactional
    public long bulkUpdateReservationsStatus(AdminUpdateReservationsRequest request) {
        if (request.status() == ReservationStatus.NO_SHOW) {
            throw new IllegalArgumentException("노쇼 경고로만 노쇼 상태로 변경할 수 있습니다.");
        }

        List<Reservation> reservationList = reservationService
                .findAllUserIdInWithUserWithLock(request.reservationIds());

        if (reservationList.isEmpty()) {
            return 0;
        }

        validateBulkReservationsUpdate(reservationList, request.status());

        return reservationService.bulkUpdateReservationsStatus(request.reservationIds(), request.status(),
                TimeUtil.nowDateTime());
    }

    // batch
    @Transactional
    public long processNoShowBatch(LocalDate today, LocalDateTime now) {
        final ReservationStatus before = ReservationStatus.PENDING;
        final ReservationStatus after = ReservationStatus.NO_SHOW;

        // 마감시간(마감시간 <= 배치 시작시간 이어야 함) 기준 노쇼 예약 조회
        List<ReservationWarnTarget> targets = reservationService.findAllByPickupDateAndStatus(today, before);
        if (targets.isEmpty()) {
            return 0;
        }

        Set<Long> targetIds = targets.stream()
            .map(ReservationWarnTarget::reservationId)
            .collect(Collectors.toSet());

        // 2. 예약 상태 일괄 변경
        long updateReservationRows = reservationService.updateAllReservationsWhereIdIn(targetIds, today, before, after,
                now);
        if (updateReservationRows != targetIds.size()) {
            throw new IllegalStateException("동시에 변경된 예약이 있어 취소/재고복원이 일치하지 않습니다.");
        }

        // 3. 변경된 예약 상태 기반 재고 복원 (또는 노쇼-마이너스 등)
        List<ProductRestockTarget> restockTargets = reservationService
                .findAllByIdInAndStatusGroupByProductIdOrderByProductId(targetIds, after);

        for (ProductRestockTarget restockTarget : restockTargets) {
            Product product = productService.findByIdWithLock(restockTarget.productId());

            product.addStock(restockTarget.quantity());
        }

        // 4. 유저 warn(노쇼 row만큼) + 메시지(1회)
        List<UserSalesRollbackTarget> userSalesRollbackTargets = reservationService
                .findUserSalesRollbackTargets(targetIds, after);

        for (UserSalesRollbackTarget target : userSalesRollbackTargets) {
            Users user = userService.findByUidWithLock(target.userUid());
            user.cancelReserve(target.totalQuantity(), target.totalAmount());

            user.warn(target.rows());
            userWarnService.noShows(user, target.rows());
            publishUserNoShowMessage(user.getUid());

            // 5. 이용제한 부여
            applyRestriction(user, today);
        }

        return updateReservationRows;
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

        if (beforeStatus == ReservationStatus.NO_SHOW) {
            throw new IllegalStateException("노쇼 경고는 한 번에 한 건만 가능합니다.");
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

    private void applyRestriction(Users user, LocalDate today) {
        int warnCount = user.getWarnCount();
        if (warnCount < 2) {
            return;
        }

        LocalDate restrictedUntil = (warnCount == 2)
                ? today.plusDays(2)
                : today.plusDays(5);

        user.restrict(restrictedUntil);

        // 6. 제한 기간 내 미래 PENDING 예약 취소
        cancelFutureReservations(user, today, restrictedUntil);
    }

    private void cancelFutureReservations(Users user, LocalDate today, LocalDate restrictedUntil) {
        List<Reservation> futureReservations = reservationService
                .findFutureReservationsByUserAndPeriod(
                        user.getUid(), ReservationStatus.PENDING,
                        today.plusDays(1), restrictedUntil);

        for (Reservation reservation : futureReservations) {
            reservation.changeStatus(ReservationStatus.CANCELED);

            Product product = productService.findByIdWithLock(reservation.getProduct().getId());
            product.addStock(reservation.getQuantity());

            user.cancelReserve(reservation.getQuantity(), reservation.getAmount());
        }
    }

    private void validateUserReservation(Users user, Reservation reservation) {
        if (!user.getUid().equals(reservation.getUser().getUid())) {
            throw new UserValidateException("다른 유저가 예약한 상품입니다.");
        }
    }

    private void publishUserNoShowMessage(String uid) {
        eventPublisher.publishEvent(UserMessageEvent.builder()
            .userUid(uid)
            .type(MessageType.USER_NO_SHOW)
            .build());
    }
}
