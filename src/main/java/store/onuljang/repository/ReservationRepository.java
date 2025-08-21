package store.onuljang.repository;


import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @EntityGraph(attributePaths = {"product"})
    List<Reservation> findAllByUserAndOrderDateBetweenOrderByOrderDateDesc(Users user, LocalDate from, LocalDate to);

    @EntityGraph(attributePaths = {"user", "product"})
    List<Reservation> findAllByOrderDate(LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id = :id")
    Optional<Reservation> findByIdWithLock(long id);
}
