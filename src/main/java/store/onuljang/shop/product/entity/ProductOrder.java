package store.onuljang.shop.product.entity;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.shared.entity.base.BaseEntity;

import java.time.LocalDate;
import java.util.*;

@NoArgsConstructor
@Builder
@Entity
@Table(name = "product_order")
public class ProductOrder extends BaseEntity {

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "product_id", nullable = false, unique = true,
        foreignKey = @ForeignKey(name = "fk_productorder_product")
    )
    private Product product;

    @Column(name = "sell_date", nullable = false)
    private LocalDate sellDate;

    @Getter
    @Setter
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Builder
    ProductOrder(Product product, LocalDate sellDate, Integer orderIndex) {
        this.product = product;
        this.sellDate = sellDate;
        this.orderIndex = orderIndex;
    }
}
