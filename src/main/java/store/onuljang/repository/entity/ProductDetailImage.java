package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import store.onuljang.repository.entity.base.BaseEntity;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_detail_images")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE product_detail_images SET deleted_at = NOW() WHERE id = ?")
public class ProductDetailImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Getter
    @Column(name = "detail_url", nullable = false)
    private String detailUrl;

    @Getter
    @Setter
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}