package store.onuljang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.repository.entity.Users;

import java.util.Optional;

public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, Long> {
    Optional<DeliveryOrder> findByIdAndUser(Long id, Users user);

    Optional<DeliveryOrder> findByUserAndIdempotencyKey(Users user, String idempotencyKey);
}
