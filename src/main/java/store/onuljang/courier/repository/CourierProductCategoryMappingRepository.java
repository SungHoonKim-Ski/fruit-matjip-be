package store.onuljang.courier.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.onuljang.courier.entity.CourierProductCategoryMapping;

public interface CourierProductCategoryMappingRepository
        extends JpaRepository<CourierProductCategoryMapping, Long> {

    @Query(
            "SELECT m FROM CourierProductCategoryMapping m"
                    + " JOIN FETCH m.courierProduct p"
                    + " WHERE m.courierProductCategory.id = :categoryId"
                    + " AND p.deletedAt IS NULL"
                    + " ORDER BY m.sortOrder ASC")
    List<CourierProductCategoryMapping> findByCategoryIdOrderBySortOrder(
            @Param("categoryId") Long categoryId);
}
