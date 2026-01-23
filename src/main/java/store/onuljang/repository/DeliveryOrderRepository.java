package store.onuljang.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.repository.entity.Users;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, Long> {
    Optional<DeliveryOrder> findByIdAndUser(Long id, Users user);

    @EntityGraph(attributePaths = {"deliveryOrderReservations", "deliveryOrderReservations.reservation",
        "deliveryOrderReservations.reservation.product", "user"})
    List<DeliveryOrder> findAllByDeliveryDate(LocalDate deliveryDate);
}
