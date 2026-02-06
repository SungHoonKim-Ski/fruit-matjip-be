package store.onuljang.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.repository.entity.DeliveryOrderReservation;
import store.onuljang.repository.entity.Reservation;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DeliveryOrderReservationRepository extends JpaRepository<DeliveryOrderReservation, Long> {
    @EntityGraph(attributePaths = {"deliveryOrder"})
    Optional<DeliveryOrderReservation> findByReservation(Reservation reservation);

    @EntityGraph(attributePaths = {"deliveryOrder"})
    List<DeliveryOrderReservation> findAllByReservationIdIn(Set<Long> reservationIds);
}
