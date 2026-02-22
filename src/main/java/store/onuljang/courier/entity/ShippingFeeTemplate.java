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
@Table(name = "shipping_fee_templates")
public class ShippingFeeTemplate extends BaseEntity {

    @Setter
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Setter
    @Column(name = "base_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseFee;

    @Setter
    @Column(name = "per_quantity_fee", precision = 12, scale = 2)
    private BigDecimal perQuantityFee;

    @Setter
    @Column(name = "free_shipping_min_amount", precision = 12, scale = 2)
    private BigDecimal freeShippingMinAmount;

    @Setter
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 템플릿 기반 배송비 계산
     *
     * @param quantity 수량
     * @param itemTotalAmount 해당 템플릿 상품들의 총 금액
     */
    public BigDecimal calculateFee(int quantity, BigDecimal itemTotalAmount) {
        // 무료배송 조건 확인
        if (freeShippingMinAmount != null
                && itemTotalAmount.compareTo(freeShippingMinAmount) >= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal fee = baseFee;
        if (perQuantityFee != null && quantity > 1) {
            fee = fee.add(perQuantityFee.multiply(BigDecimal.valueOf(quantity - 1)));
        }
        return fee;
    }
}
