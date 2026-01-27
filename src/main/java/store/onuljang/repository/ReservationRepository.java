package store.onuljang.repository;

import org.springframework.data.jpa.repository.*;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = {"user", "product"})
    List<Reservation> findAllByPickupDate(LocalDate date);

    @EntityGraph(attributePaths = {"user"})
    List<Reservation> findAllByIdIn(Set<Long> ids);
}
