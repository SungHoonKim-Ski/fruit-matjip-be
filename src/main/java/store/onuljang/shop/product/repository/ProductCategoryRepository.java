package store.onuljang.shop.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import store.onuljang.shop.product.entity.ProductCategory;

import java.util.List;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    List<ProductCategory> findAllByOrderBySortOrderAsc();


    List<ProductCategory> findAllByName(String name);

    @Modifying(flushAutomatically = true)
    void deleteByName(String name);
}
