package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.onuljang.exception.ProductExceedException;
import store.onuljang.exception.ProductUnavailableException;
import store.onuljang.repository.entity.base.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
@SQLDelete(sql = "UPDATE products SET deleted_at = NOW() WHERE id = ?")
public class ProductAll extends BaseEntity {
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
    private Integer stock;

    @Getter
    @Setter
    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Getter
    @Setter
    @Column(name = "sell_date", nullable = false)
    private LocalDate sellDate;

    @Getter
    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible;

    @Getter
    @Column(name = "total_sold", nullable = false)
    @Builder.Default
    private Long totalSold;

    @Getter
    @OneToOne(fetch = FetchType.LAZY, optional = false)
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

    @OrderBy("sortOrder ASC")
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductDetailImage> productDetailImages = new ArrayList<>();
}