package store.onuljang.repository.entity;

import java.math.BigDecimal;

public interface ReservationSalesRow {
    Long getProductId();
    String getProductName();
    Integer getQuantity();
    BigDecimal getAmount();
}