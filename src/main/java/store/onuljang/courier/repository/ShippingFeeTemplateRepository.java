package store.onuljang.courier.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.courier.entity.ShippingFeeTemplate;

public interface ShippingFeeTemplateRepository extends JpaRepository<ShippingFeeTemplate, Long> {
    List<ShippingFeeTemplate> findAllByOrderBySortOrderAsc();

    List<ShippingFeeTemplate> findByActiveTrueOrderBySortOrderAsc();
}
