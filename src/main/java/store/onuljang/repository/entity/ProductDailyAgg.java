package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.repository.entity.base.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_daily_agg")
public class ProductDailyAgg extends BaseEntity {
    @Getter
    @Setter
    @Column(name = "sell_date", nullable = false)
    private LocalDate sellDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductAll product;

    @Getter
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Getter
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    public String getProductName() {
        return product.getName();
    }
}
