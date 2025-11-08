package store.onuljang.repository;


import org.springframework.data.jpa.repository.*;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @EntityGraph(attributePaths = {"product"})
    List<Reservation> findAllByUserAndPickupDateBetweenOrderByPickupDateDesc(Users user, LocalDate from, LocalDate to);

    @EntityGraph(attributePaths = {"user", "product"})
    List<Reservation> findAllByPickupDate(LocalDate date);

}
