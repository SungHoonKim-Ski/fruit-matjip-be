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
@Table(name = "courier_product_options")
public class CourierProductOption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_group_id", nullable = false)
    private CourierProductOptionGroup optionGroup;

    @Setter
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Setter
    @Column(name = "additional_price", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal additionalPrice = BigDecimal.ZERO;

    @Setter
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
