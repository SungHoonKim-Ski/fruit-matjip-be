package store.onuljang.courier.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
import store.onuljang.shared.entity.base.BaseEntity;
import store.onuljang.shared.entity.enums.CourierOrderItemStatus;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "courier_order_items")
public class CourierOrderItem extends BaseEntity {

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "courier_order_id", nullable = false)
    private CourierOrder courierOrder;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_product_id")
    private CourierProduct courierProduct;

    @Getter
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Getter
    @Column(name = "product_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal productPrice;

    @Getter
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Getter
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Getter
    @Lob
    @Column(name = "selected_options", columnDefinition = "TEXT")
    private String selectedOptions;

    @Getter
    @Column(name = "selected_option_ids", length = 500)
    private String selectedOptionIds;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "item_status", nullable = false, length = 20)
    @Builder.Default
    private CourierOrderItemStatus itemStatus = CourierOrderItemStatus.NORMAL;

    public void markClaimRequested() {
        this.itemStatus = CourierOrderItemStatus.CLAIM_REQUESTED;
    }

    public void markClaimResolved() {
        this.itemStatus = CourierOrderItemStatus.CLAIM_RESOLVED;
    }

    public void markRefunded() {
        this.itemStatus = CourierOrderItemStatus.REFUNDED;
    }
}
