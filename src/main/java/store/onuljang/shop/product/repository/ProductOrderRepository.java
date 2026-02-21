package store.onuljang.shop.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import store.onuljang.shop.product.entity.ProductOrder;

import java.time.LocalDate;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteAllBySellDate(LocalDate sellDate);
}
