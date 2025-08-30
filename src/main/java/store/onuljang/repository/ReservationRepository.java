package store.onuljang.repository;


import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import store.onuljang.repository.entity.ProductRestockTarget;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @EntityGraph(attributePaths = {"product"})
    List<Reservation> findAllByUserAndPickupDateBetweenOrderByPickupDateDesc(Users user, LocalDate from, LocalDate to);

    @EntityGraph(attributePaths = {"user", "product"})
    List<Reservation> findAllByPickupDate(LocalDate date);

    @Query("""
    select r.id
    from Reservation r
    where
        r.pickupDate = :pickupDate
        and r.status = :status
    """)
    Set<Long> findIdsByPickupDateAndStatus(LocalDate pickupDate, ReservationStatus status);

    @Query("""
    select
        r.product.id as productId,
        sum(r.quantity) as quantity
    from Reservation r
    where
        r.id in (:ids)
        and r.status = :status
    group by r.product.id
    order by r.product.id
    """)
    List<ProductRestockTarget> findAllByIdInAndStatusGroupByProductIdOrderByProductId(
        @Param("ids") Set<Long> ids,
        @Param("status") ReservationStatus status
    );

    @Query(
        "select r " +
        "from Reservation r " +
        "left join fetch r.user u " +
        "where r.id in (:ids)"
    )
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Reservation> findAllByIdInWithUserWithLock(@Param("ids") Set<Long> ids);

    @Query(
        "select r " +
        "from Reservation r " +
        "where r.id = :id"
    )
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Reservation> findByIdWithLock(@Param("id") long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update Reservation r
        set r.status = :status,
            r.statusChangedAt = :now
        where r.id in (:ids)
    """)
    int updateStatusIdIn(
        @Param("ids") Set<Long> ids,
        @Param("status") ReservationStatus status,
        @Param("now") LocalDateTime now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update Reservation r
        set r.status = :after,
            r.statusChangedAt = :now
        where r.id in (:ids)
        and r.status = :before
        and r.pickupDate = :today
    """)
    int updateAllReservationStatus(
        @Param("ids") Set<Long> ids,
        @Param("today") LocalDate today,
        @Param("before") ReservationStatus before,
        @Param("after") ReservationStatus after,
        @Param("now") LocalDateTime now
    );
}
