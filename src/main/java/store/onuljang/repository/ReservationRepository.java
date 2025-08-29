package store.onuljang.repository;


import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
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

    @Query(
        "select r " +
        "from Reservation r " +
        "left join fetch r.user u " +
        "where r.id in :ids"
    )
    List<Reservation> findAllByIdInWithUser(Set<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        "select r " +
        "from Reservation r " +
        "where r.id = :id"
    )
    Optional<Reservation> findByIdWithLock(@Param("id") long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update Reservation r
        set r.status = :status,
            r.statusChangedAt = :now
        where r.id in :ids
    """)
    int updateStatusIdIn(
        @Param("ids") Set<Long> ids,
        @Param("status") ReservationStatus status,
        @Param("now") LocalDateTime now
    );
}
