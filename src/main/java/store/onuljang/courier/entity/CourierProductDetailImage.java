package store.onuljang.courier.entity;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.shared.entity.base.BaseEntity;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "courier_product_detail_images")
public class CourierProductDetailImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_product_id", nullable = false)
    private CourierProduct courierProduct;

    @Getter
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Getter
    @Setter
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
