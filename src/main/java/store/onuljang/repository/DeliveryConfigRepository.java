package store.onuljang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.repository.entity.DeliveryConfig;

import java.util.Optional;

public interface DeliveryConfigRepository extends JpaRepository<DeliveryConfig, Long> {
    Optional<DeliveryConfig> findTopByOrderByIdAsc();
}
