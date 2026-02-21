package store.onuljang.courier.entity;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.shared.entity.base.BaseEntity;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Table(
        name = "courier_product_category_mapping",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uix_cpcm_product_category",
                        columnNames = {"courier_product_id", "category_id"}))
public class CourierProductCategoryMapping extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "courier_product_id", nullable = false)
    private CourierProduct courierProduct;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CourierProductCategory courierProductCategory;

    @Setter
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
