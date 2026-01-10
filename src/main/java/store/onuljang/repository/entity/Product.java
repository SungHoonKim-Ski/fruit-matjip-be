package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.onuljang.exception.ProductExceedException;
import store.onuljang.exception.ProductUnavailableException;
import store.onuljang.repository.entity.base.BaseEntity;
import jakarta.persistence.OrderBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    @Builder.Default
    private Integer stock = 0;

    @Getter
    @Setter
    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Getter
    @Setter
    @Column(name = "sell_date", nullable = false)
    private LocalDate sellDate;

    @Getter
    @Column(name = "sell_time")
    private LocalTime sellTime;

    @Getter
    @Column(name = "visible", nullable = false)
    @Builder.Default
    private Boolean visible = true;

    @Getter
    @Column(name = "self_pick", nullable = false)
    @Builder.Default
    private Boolean selfPick = true;

    @Getter
    @Column(name = "total_sold", nullable = false)
    @Builder.Default
    private Long totalSold = 0L;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registered_admin", nullable = false)
    private Admin registeredAdmin;

    @OneToOne(mappedBy = "product", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private ProductOrder productOrder;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Lob
    @Getter
    @Column(columnDefinition = "TEXT")
    @Setter
    private String description;

    @Builder.Default
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

    public void setStock(int stock) {
        this.stock = stock;
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

    public void toggleSelfPick() {
        this.selfPick = !this.selfPick;
    }

    public void toggleVisible() {
        this.visible = !this.visible;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.productDetailImages.forEach(ProductDetailImage::delete);
    }

    public void assertPurchasable(int quantity) {
        if (Boolean.FALSE.equals(visible)) {
            throw new ProductUnavailableException("판매가 중단된 상품입니다.");
        }
        if (quantity <= 0) {
            throw new ProductUnavailableException("구매 수량은 1개 이상이어야 합니다.");
        }
        if (this.stock < quantity) {
            throw new ProductExceedException("상품의 재고가 부족합니다.");
        }
    }

    public void reserve(int quantity) {
        assertPurchasable(quantity);
        removeStock(quantity);
        addTotalSold(quantity);
    }

    public void cancel(int quantity) {
        addStock(quantity);
        removeTotalSold(quantity);
    }

    public void setProductOrder(ProductOrder productOrder) {
        this.productOrder = productOrder;
        if (productOrder != null && productOrder.getProduct() != this) {
            productOrder.setProduct(this);
        }
    }

    public int getOrderIndex() {
        if (this.productOrder == null) {
            return 0;
        } else {
            return this.productOrder.getOrderIndex();
        }
    }

    public void setSellTime(LocalTime sellTime) {
        this.sellTime = sellTime;
    }

    public void assertCanSelfPick() {
        if (!this.selfPick) {
            throw new ProductUnavailableException("셀프 수령이 불가능한 상품입니다.");
        }
    }

    private void removeTotalSold(int quantity) {
        this.totalSold = Math.max(this.totalSold - quantity, 0);
    }

    private void addTotalSold(int quantity) {
        this.totalSold += quantity;
    }
}
