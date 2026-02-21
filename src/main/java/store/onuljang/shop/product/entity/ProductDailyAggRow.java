package store.onuljang.shop.product.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ProductDailyAggRow {
    LocalDate getSellDate();
    Integer getQuantity();
    BigDecimal getAmount();
}
