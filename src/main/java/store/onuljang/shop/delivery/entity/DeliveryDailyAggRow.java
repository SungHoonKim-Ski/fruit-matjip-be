package store.onuljang.shop.delivery.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DeliveryDailyAggRow {
    LocalDate getSellDate();

    Integer getOrderCount();

    Integer getQuantity();

    BigDecimal getAmount();

    BigDecimal getDeliveryFee();
}
