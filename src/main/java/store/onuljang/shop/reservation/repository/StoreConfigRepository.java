package store.onuljang.shop.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.shop.reservation.entity.StoreConfig;

import java.util.Optional;

public interface StoreConfigRepository extends JpaRepository<StoreConfig, Long> {
    Optional<StoreConfig> findTopByOrderByIdAsc();
}
