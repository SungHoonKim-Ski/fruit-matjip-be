package store.onuljang.courier.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import store.onuljang.courier.entity.CourierProductCategory;

public interface CourierProductCategoryRepository
        extends JpaRepository<CourierProductCategory, Long> {
    List<CourierProductCategory> findAllByOrderBySortOrderAsc();

    List<CourierProductCategory> findAllByName(String name);

    @Modifying(flushAutomatically = true)
    void deleteByName(String name);
}
