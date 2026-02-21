package store.onuljang.courier.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.onuljang.courier.entity.CourierProduct;

public interface CourierProductRepository extends JpaRepository<CourierProduct, Long> {

    @Query("SELECT p FROM CourierProduct p WHERE p.visible = true AND p.deletedAt IS NULL ORDER BY p.sortOrder ASC")
    List<CourierProduct> findAllVisible();

    @Query(
            "SELECT DISTINCT p FROM CourierProduct p"
                    + " JOIN p.productCategories c"
                    + " WHERE p.visible = true AND p.deletedAt IS NULL AND c.id = :categoryId"
                    + " ORDER BY p.sortOrder ASC")
    List<CourierProduct> findAllVisibleByCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT p FROM CourierProduct p WHERE p.deletedAt IS NULL ORDER BY p.sortOrder ASC")
    List<CourierProduct> findAllNotDeleted();

    @Query(
            "SELECT p FROM CourierProduct p"
                    + " LEFT JOIN FETCH p.detailImages"
                    + " WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<CourierProduct> findByIdWithDetailImages(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM CourierProduct p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<CourierProduct> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT p FROM CourierProduct p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<CourierProduct> findByIdAndNotDeleted(@Param("id") Long id);
}
