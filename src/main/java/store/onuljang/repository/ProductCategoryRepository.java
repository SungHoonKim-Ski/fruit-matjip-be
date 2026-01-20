package store.onuljang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import store.onuljang.repository.entity.ProductCategory;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    List<ProductCategory> findAllByOrderBySortOrderAsc();

    List<ProductCategory> findAllByOrderByCreatedAtAsc();

    Optional<ProductCategory> findByName(String name);

    List<ProductCategory> findAllByName(String name);

    boolean existsByName(String name);

    @Modifying(flushAutomatically = true)
    void deleteByName(String name);

    @Modifying(flushAutomatically = true)
    @Query("delete from ProductCategory")
    void deleteAllCategories();
}
