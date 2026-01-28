package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.exception.NotFoundException;
import store.onuljang.repository.ReservationAllRepository;
import store.onuljang.repository.ReservationQueryRepository;
import store.onuljang.repository.ReservationRepository;
import store.onuljang.repository.entity.*;
import store.onuljang.repository.entity.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@Transactional(readOnly = true)
public class ReservationService {
    ReservationRepository reservationRepository;
    ReservationQueryRepository reservationQueryRepository;
    ReservationAllRepository reservationAllRepository;

    @Transactional
    public Reservation findByIdWithLock(long id) {
        return reservationQueryRepository.findByIdWithLock(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));
    }

    @Transactional
    public long save(Reservation reservation) {
        reservationRepository.save(reservation);

        return reservation.getId();
    }

    @Transactional
    public long bulkUpdateReservationsStatus(Set<Long> reservationIdSet, ReservationStatus updateStatus,
            LocalDateTime updateTime) {
        return reservationQueryRepository.updateStatusIdIn(reservationIdSet, updateStatus, updateTime);
    }

    @Transactional
    public long updateAllReservationsWhereIdIn(Set<Long> reservationIdSet, LocalDate today, ReservationStatus before,
            ReservationStatus after, LocalDateTime now) {
        return reservationQueryRepository.updateAllReservationStatus(reservationIdSet, today, before, after, now);
    }

    @Transactional(readOnly = true)
    public List<ProductRestockTarget> findAllByIdInAndStatusGroupByProductIdOrderByProductId(Set<Long> reservationIdSet,
            ReservationStatus status) {
        return reservationQueryRepository.findAllByIdInAndStatusGroupByProductIdOrderByProductId(reservationIdSet,
                status);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllUserIdInWithUserWithLock(Set<Long> reservationIdSet) {
        return reservationQueryRepository.findAllByIdInWithUserWithLock(reservationIdSet);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllUserIdInWithUser(Set<Long> reservationIdSet) {
        return reservationRepository.findAllByIdIn(reservationIdSet);
    }

    @Transactional(readOnly = true)
    public Reservation findById(long id) {
        return reservationRepository.findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));
    }

    @Transactional(readOnly = true)
    public List<Reservation> finAllByDateWithUserAndProduct(LocalDate date) {
        return reservationRepository.findAllByPickupDate(date);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllByUserAndPickupDateBetweenWithProductAllAndDeliveryOrderByPickupDateDesc(Users user,
             LocalDate from, LocalDate to) {
        return reservationQueryRepository.findAllByUserAndPickupDateBetweenWithProductAllAndDelivery(user, from, to);
    }

    @Transactional(readOnly = true)
    public List<ReservationSalesRow> findPickupDateSales(Set<ReservationStatus> status, LocalDate date) {
        return reservationAllRepository.findPickupDateSales(status.stream().map(Enum::name).toList(), date);
    }

    @Transactional(readOnly = true)
    public List<ReservationWarnTarget> findAllByPickupDateAndStatus(LocalDate today, ReservationStatus status) {
        return reservationQueryRepository.findWarnTargetsByPickupDateAndStatus(today, status);
    }

    @Transactional(readOnly = true)
    public List<UserSalesRollbackTarget> findUserSalesRollbackTargets(Set<Long> reservationIdSet,
            ReservationStatus status) {
        return reservationQueryRepository.findUserSalesRollbackTargets(reservationIdSet, status);
    }
}
