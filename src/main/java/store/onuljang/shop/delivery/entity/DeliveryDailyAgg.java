package store.onuljang.shop.delivery.entity;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.shared.entity.base.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "delivery_daily_agg")
public class DeliveryDailyAgg extends BaseEntity {
    @Getter
    @Column(name = "sell_date", nullable = false)
    private LocalDate sellDate;

    @Getter
    @Column(name = "order_count", nullable = false)
    private Integer orderCount;

    @Getter
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Getter
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Getter
    @Column(name = "delivery_fee", nullable = false)
    private BigDecimal deliveryFee;
}
