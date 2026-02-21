package store.onuljang.shop.reservation.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store.onuljang.shared.user.entity.*;
import store.onuljang.shop.product.entity.*;
import store.onuljang.shop.reservation.entity.*;
import store.onuljang.shop.delivery.entity.*;
import store.onuljang.shop.admin.entity.*;
import store.onuljang.shared.auth.entity.*;
import store.onuljang.shared.repository.entity.*;
import store.onuljang.shared.entity.enums.*;
import store.onuljang.shared.entity.base.*;
import store.onuljang.shared.entity.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static store.onuljang.shop.product.entity.QProductAll.productAll;
import static store.onuljang.shop.reservation.entity.QReservation.reservation;

@Repository
@RequiredArgsConstructor
public class ReservationQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<ReservationWarnTarget> findWarnTargetsByPickupDateAndStatus(
            LocalDate pickupDate
            , ReservationStatus status
    ) {
        return queryFactory
            .select(Projections.constructor(
                ReservationWarnTarget.class,
                reservation.id,
                reservation.user.uid
            ))
            .from(reservation)
            .where(
                reservation.pickupDate.eq(pickupDate),
                reservation.status.eq(status)
            )
            .fetch();
    }

    public List<ProductRestockTarget> findAllByIdInAndStatusGroupByProductIdOrderByProductId(
        Set<Long> ids
        , ReservationStatus status
    ) {
        return queryFactory
            .select(Projections.constructor(
                ProductRestockTarget.class,
                reservation.product.id,
                reservation.quantity.sum()
            ))
            .from(reservation)
            .where(
                reservation.id.in(ids),
                reservation.status.eq(status)
            )
            .groupBy(reservation.product.id)
            .orderBy(reservation.product.id.asc())
            .fetch();
    }

    public List<UserSalesRollbackTarget> findUserSalesRollbackTargets(
        Set<Long> ids
        , ReservationStatus status
    ) {
        return queryFactory
            .select(Projections.constructor(
                UserSalesRollbackTarget.class,
                reservation.user.uid,
                reservation.quantity.sum(),
                reservation.amount.sum(),
                reservation.id.countDistinct().intValue()
            ))
            .from(reservation)
            .where(
                reservation.id.in(ids),
                reservation.status.eq(status)
            )
            .groupBy(reservation.user.uid)
            .fetch();
    }

    public List<Reservation> findAllByIdInWithUserWithLock(Set<Long> ids) {
        return queryFactory
            .selectFrom(reservation)
            .leftJoin(reservation.user).fetchJoin()
            .where(reservation.id.in(ids))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .fetch();
    }

    public List<Reservation> findAllByUserAndPickupDateBetweenWithProductAllAndDelivery(Users user, LocalDate from, LocalDate to) {
        return queryFactory
            .selectFrom(reservation)
            .leftJoin(reservation.productAll, productAll).fetchJoin()
            .leftJoin(reservation.deliveryOrderReservation).fetchJoin()
            .leftJoin(reservation.deliveryOrderReservation.deliveryOrder).fetchJoin()
            .where(
                reservation.user.eq(user),
                reservation.pickupDate.between(from, to)
            )
            .orderBy(reservation.pickupDate.desc())
            .fetch();
    }

    public Optional<Reservation> findByIdWithLock(long id) {
        Reservation result = queryFactory
            .selectFrom(reservation)
            .where(reservation.id.eq(id))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .fetchOne();

        return Optional.ofNullable(result);
    }

    public long updateStatusIdIn(
        Set<Long> ids,
        ReservationStatus status,
        LocalDateTime now
    ) {
        return queryFactory
            .update(reservation)
            .set(reservation.status, status)
            .set(reservation.statusChangedAt, now)
            .where(reservation.id.in(ids))
            .execute();
    }

    public long updateAllReservationStatus(
        Set<Long> ids,
        LocalDate today,
        ReservationStatus before,
        ReservationStatus after,
        LocalDateTime now
    ) {
        return queryFactory
            .update(reservation)
            .set(reservation.status, after)
            .set(reservation.statusChangedAt, now)
            .where(
                reservation.id.in(ids),
                reservation.status.eq(before),
                reservation.pickupDate.eq(today)
            )
            .execute();
    }
}
