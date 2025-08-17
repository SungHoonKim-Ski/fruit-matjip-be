package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.onuljang.repository.entity.base.BaseEntity;
import jakarta.persistence.OrderBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE products SET deleted_at = NOW() WHERE id = ?")
public class Product extends BaseEntity {
    @Getter
    @Setter
    @Column(name = "product_url", nullable = false)
    private String productUrl;

    @Getter
    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Getter
    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Getter
    @Setter
    @Column(name = "price", nullable = false)
    private Integer price;

    @Getter
    @Setter
    @Column(name = "sell_date", nullable = false)
    private LocalDate sellDate;

    @Getter
    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = true;

    @Getter
    @Column(name = "total_sold", nullable = false)
    @Builder.Default
    private Long totalSold = 0L;

    @Getter
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registered_admin", nullable = false)
    private Admin registeredAdmin;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Lob
    @Getter
    @Column(columnDefinition = "TEXT")
    @Setter
    private String description;

    @OrderBy("sortOrder ASC")
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductDetailImage> productDetailImages = new ArrayList<>();

    public List<String> getDetailImages() {
        if (this.productDetailImages == null) {
            return List.of();
        }

        return this.productDetailImages.stream()
            .map(ProductDetailImage::getDetailUrl)
            .toList();
    }

    public void addStock(int quantity) {
        this.stock = Math.max(this.stock + quantity, 0);
    }

    public void removeStock(int quantity) {
        this.stock = Math.max(this.stock - quantity, 0);
    }

    public List<String> replaceDetailImagesInOrder(List<String> replaceDetailImages) {
        if (replaceDetailImages == null) return List.of();

        LinkedHashSet<String> updateSet = new LinkedHashSet<>();
        for (String url : replaceDetailImages) {
            if (url == null) continue;
            String detailImage = url.trim();
            if (!detailImage.isEmpty()) {
                updateSet.add(detailImage);
            }
        }

        Map<String, ProductDetailImage> oldMap = new HashMap<>();
        for (ProductDetailImage productDetailImage : this.productDetailImages) {
            oldMap.put(productDetailImage.getDetailUrl(), productDetailImage);
        }

        List<String> removeKeys = new ArrayList<>();
        this.productDetailImages.removeIf(productDetailImage -> {
            boolean remove = !updateSet.contains(productDetailImage.getDetailUrl());
            if (remove) {
                removeKeys.add(productDetailImage.getDetailUrl());
                productDetailImage.delete();
            }
            return remove;
        });

        int order = 1;
        for (String key : updateSet) {
            ProductDetailImage img = oldMap.get(key);
            if (img == null) {
                img = ProductDetailImage.builder()
                    .product(this)
                    .detailUrl(key)
                    .sortOrder(order)
                    .build();
                this.productDetailImages.add(img);
            } else {
                img.setSortOrder(order);
            }
            order++;
        }

        return removeKeys;
    }

    public void soldOut() {
        this.stock = 0;
    }

    public void toggleVisible() {
        this.isVisible = !this.isVisible;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}