package store.onuljang.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByUserAndOrderDateBetween(Users user, LocalDate from, LocalDate to);
}
