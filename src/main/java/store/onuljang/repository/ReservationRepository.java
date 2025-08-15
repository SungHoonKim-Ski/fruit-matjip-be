package store.onuljang.repository;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.ReservationStatus;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @EntityGraph(attributePaths = {"product"})
    List<Reservation> findAllByUserAndOrderDateBetween(Users user, LocalDate from, LocalDate to);

    @EntityGraph(attributePaths = {"user", "product"})
    List<Reservation> findAllByOrderDate(LocalDate date);

    List<Reservation> findAllByStatusAndOrderDateBetween(ReservationStatus status, LocalDate from, LocalDate to);
}
