package store.onuljang.shop.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.shop.delivery.entity.DeliveryConfig;

import java.util.Optional;

public interface DeliveryConfigRepository extends JpaRepository<DeliveryConfig, Long> {
    Optional<DeliveryConfig> findTopByOrderByIdAsc();
}
