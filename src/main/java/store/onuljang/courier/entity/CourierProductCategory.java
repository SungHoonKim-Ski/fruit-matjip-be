package store.onuljang.courier.entity;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.shared.entity.base.BaseLogEntity;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Table(name = "courier_product_category")
public class CourierProductCategory extends BaseLogEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    public void setName(String name) {
        this.name = name;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
