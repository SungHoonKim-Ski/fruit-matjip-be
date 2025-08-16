package store.onuljang.repository;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.ReservationAll;
import store.onuljang.repository.entity.enums.ReservationStatus;

import java.time.LocalDate;
import java.util.List;

public interface ReservationAllRepository extends JpaRepository<ReservationAll, Long> {
    @EntityGraph(attributePaths = {"user", "product"})
    List<ReservationAll> findAllByStatusAndOrderDateBetween(ReservationStatus status, LocalDate from, LocalDate to);
}
