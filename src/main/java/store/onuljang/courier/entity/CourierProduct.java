package store.onuljang.courier.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import store.onuljang.shared.entity.base.BaseEntity;
import store.onuljang.shared.util.TimeUtil;
import store.onuljang.shop.admin.entity.Admin;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "courier_products")
@SQLRestriction("deleted_at IS NULL")
public class CourierProduct extends BaseEntity {

    @Getter
    @Setter
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Getter
    @Setter
    @Column(name = "product_url", nullable = false, length = 500)
    private String productUrl;

    @Getter
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Getter
    @Column(name = "visible", nullable = false)
    @Builder.Default
    private Boolean visible = true;

    @Getter
    @Column(name = "sold_out", nullable = false)
    @Builder.Default
    private Boolean soldOut = false;

    @Getter
    @Setter
    @Column(name = "weight_gram")
    private Integer weightGram;

    @Getter
    @Setter
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Getter
    @Setter
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Getter
    @Setter
    @Column(name = "recommended", nullable = false)
    @Builder.Default
    private Boolean recommended = false;

    @Getter
    @Setter
    @Column(name = "recommend_order", nullable = false)
    @Builder.Default
    private Integer recommendOrder = 0;

    @Getter
    @Column(name = "total_sold", nullable = false)
    @Builder.Default
    private Long totalSold = 0L;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin registeredAdmin;

    @Builder.Default
    @OneToMany(mappedBy = "courierProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<CourierProductCategoryMapping> categoryMappings = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "courierProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<CourierProductOptionGroup> optionGroups = new ArrayList<>();

    @Getter
    @Setter
    @Column(name = "shipping_fee", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Getter
    @Setter
    @Column(name = "combined_shipping_quantity", nullable = false)
    @Builder.Default
    private Integer combinedShippingQuantity = 1;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public List<CourierProductOptionGroup> getOptionGroups() {
        if (this.optionGroups == null) {
            return List.of();
        }
        return this.optionGroups;
    }

    public void replaceOptionGroups(List<CourierProductOptionGroup> newGroups) {
        this.optionGroups.clear();
        if (newGroups != null) {
            this.optionGroups.addAll(newGroups);
        }
    }

    public Set<CourierProductCategory> getProductCategories() {
        if (categoryMappings == null) return Set.of();
        return categoryMappings.stream()
                .map(CourierProductCategoryMapping::getCourierProductCategory)
                .collect(Collectors.toSet());
    }

    public void assertPurchasable(int quantity) {
        if (deletedAt != null) {
            throw new IllegalStateException("삭제된 상품입니다.");
        }
        if (Boolean.FALSE.equals(visible)) {
            throw new IllegalStateException("판매가 중단된 상품입니다.");
        }
        if (Boolean.TRUE.equals(soldOut)) {
            throw new IllegalStateException("품절된 상품입니다.");
        }
        if (quantity <= 0) {
            throw new IllegalStateException("구매 수량은 1개 이상이어야 합니다.");
        }
    }

    public void purchase(int quantity) {
        assertPurchasable(quantity);
        this.totalSold += quantity;
    }

    public void restoreStock(int quantity) {
        // stock field removed — no-op
    }

    public boolean isAvailable() {
        return Boolean.TRUE.equals(visible) && !Boolean.TRUE.equals(soldOut) && deletedAt == null;
    }

    public void softDelete() {
        this.deletedAt = TimeUtil.nowDateTime();
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void toggleVisible() {
        this.visible = !this.visible;
    }

    public void toggleSoldOut() {
        this.soldOut = !this.soldOut;
    }

    public void toggleRecommended() {
        this.recommended = !this.recommended;
    }

    public void updateCategories(Set<CourierProductCategory> categories) {
        this.categoryMappings.clear();
        if (categories != null) {
            int idx = 0;
            for (CourierProductCategory cat : categories) {
                this.categoryMappings.add(
                        CourierProductCategoryMapping.builder()
                                .courierProduct(this)
                                .courierProductCategory(cat)
                                .sortOrder(idx++)
                                .build());
            }
        }
    }
}
