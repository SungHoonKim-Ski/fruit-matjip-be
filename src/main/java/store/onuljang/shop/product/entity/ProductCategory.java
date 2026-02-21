package store.onuljang.shop.product.entity;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.shared.entity.base.BaseLogEntity;

import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Table(name = "product_category")
public class ProductCategory extends BaseLogEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @ManyToMany
    @JoinTable(name = "product_category_mapping",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    @Builder.Default
    private Set<Product> products = new HashSet<>();

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
