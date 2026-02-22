package store.onuljang.courier.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
import store.onuljang.shared.entity.base.BaseEntity;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Table(name = "shipping_fee_policies")
public class ShippingFeePolicy extends BaseEntity {

    @Column(name = "min_quantity", nullable = false)
    private Integer minQuantity;

    @Column(name = "max_quantity", nullable = false)
    private Integer maxQuantity;

    @Column(name = "fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal fee;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    public void update(
            Integer minQuantity,
            Integer maxQuantity,
            BigDecimal fee,
            Integer sortOrder) {
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.fee = fee;
        this.sortOrder = sortOrder;
    }
}
