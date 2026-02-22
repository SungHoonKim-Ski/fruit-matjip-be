package store.onuljang.courier.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.onuljang.courier.entity.ShippingFeePolicy;

public interface ShippingFeePolicyRepository extends JpaRepository<ShippingFeePolicy, Long> {

    @Query(
            "SELECT s FROM ShippingFeePolicy s"
                    + " WHERE s.minQuantity <= :qty AND s.maxQuantity >= :qty"
                    + " ORDER BY s.sortOrder ASC")
    List<ShippingFeePolicy> findAllByQuantityRange(@Param("qty") int qty);

    List<ShippingFeePolicy> findAllByOrderBySortOrderAsc();
}
