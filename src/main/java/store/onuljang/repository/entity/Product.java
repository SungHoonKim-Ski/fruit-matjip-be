package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.repository.entity.base.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    @Getter
    @Column(name = "product_url", nullable = false)
    private String productUrl;

    @Getter
    @Column(name = "name", nullable = false)
    private String name;

    @Getter
    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Getter
    @Column(name = "price", nullable = false)
    private Integer price;

    @Getter
    @Column(name = "sell_date", nullable = false)
    private LocalDate sellDate;

    @Getter
    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = true;

    @Getter
    @Column(name = "total_sold", nullable = false)
    private Long totalSold = 0L;

    @Getter
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registered_admin", nullable = false)
    private Admin registeredAdmin;

    @Getter
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Lob
    @Getter
    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductDetailImage> productDetailImages = new ArrayList<>();

    public List<String> getDetailImages() {
        if (productDetailImages == null) {
            return List.of();
        }

        return this.productDetailImages.stream()
            .map(ProductDetailImage::getDetailUrl)
            .toList();
    }
}