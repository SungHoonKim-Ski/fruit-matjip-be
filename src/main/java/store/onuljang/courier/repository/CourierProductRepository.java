package store.onuljang.courier.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.onuljang.courier.entity.CourierProduct;

public interface CourierProductRepository extends JpaRepository<CourierProduct, Long> {

    List<CourierProduct> findByVisibleTrueOrderBySortOrderAsc();

    @Query(
            "SELECT DISTINCT p FROM CourierProduct p"
                    + " JOIN p.categoryMappings m"
                    + " WHERE p.visible = true AND m.courierProductCategory.id = :categoryId"
                    + " ORDER BY m.sortOrder ASC")
    List<CourierProduct> findAllVisibleByCategory(@Param("categoryId") Long categoryId);

    List<CourierProduct> findAllByOrderBySortOrderAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM CourierProduct p WHERE p.id = :id")
    Optional<CourierProduct> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM CourierProduct p"
            + " LEFT JOIN FETCH p.optionGroups"
            + " WHERE p.id = :id")
    Optional<CourierProduct> findByIdWithOptionGroups(@Param("id") Long id);

    @Query("SELECT p FROM CourierProduct p WHERE p.recommended = true AND p.visible = true ORDER BY p.recommendOrder ASC")
    List<CourierProduct> findRecommendedProducts();

    @Query("SELECT p FROM CourierProduct p WHERE p.visible = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY p.sortOrder ASC")
    List<CourierProduct> findByNameContaining(@Param("keyword") String keyword);

    @Query("SELECT p FROM CourierProduct p JOIN p.categoryMappings m WHERE p.visible = true AND m.courierProductCategory.id = :categoryId ORDER BY m.sortOrder ASC")
    List<CourierProduct> findVisibleByCategoryOrdered(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT COALESCE(MIN(p.sortOrder), 0) FROM CourierProduct p WHERE p.deletedAt IS NULL")
    int findMinSortOrder();
}
