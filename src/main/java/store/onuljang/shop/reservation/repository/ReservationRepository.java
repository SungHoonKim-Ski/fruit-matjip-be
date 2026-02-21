package store.onuljang.shop.reservation.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import store.onuljang.shop.reservation.entity.Reservation;
import store.onuljang.shared.entity.enums.ReservationStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = {"user", "product"})
    List<Reservation> findAllByPickupDate(LocalDate date);

    @EntityGraph(attributePaths = {"user"})
    List<Reservation> findAllByIdIn(Set<Long> ids);

    @EntityGraph(attributePaths = {"product"})
    List<Reservation> findByUserUidAndStatusAndPickupDateBetween(
            String uid, ReservationStatus status, LocalDate from, LocalDate to);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.displayCode = :displayCode")
    Optional<Reservation> findByDisplayCodeWithLock(@Param("displayCode") String displayCode);

    @EntityGraph(attributePaths = {"user"})
    List<Reservation> findAllByDisplayCodeIn(Collection<String> displayCodes);

    boolean existsByDisplayCode(String displayCode);
}
