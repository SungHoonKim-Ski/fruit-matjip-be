package store.onuljang.courier.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.onuljang.courier.entity.ShippingFeePolicy;

public interface ShippingFeePolicyRepository extends JpaRepository<ShippingFeePolicy, Long> {

    @Query(
            "SELECT s FROM ShippingFeePolicy s WHERE s.active = true"
                    + " AND s.minQuantity <= :qty AND s.maxQuantity >= :qty")
    Optional<ShippingFeePolicy> findByQuantityRange(@Param("qty") int qty);

    List<ShippingFeePolicy> findAllByOrderBySortOrderAsc();
}
