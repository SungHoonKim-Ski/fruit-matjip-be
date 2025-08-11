// store/onuljang/repository/entity/AdminProductLog.java
package store.onuljang.repository.entity.log;

import jakarta.persistence.*;
import lombok.*;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.base.BaseLogEntity;

@NoArgsConstructor
@Entity
@Table(name = "admin_product_logs")
public class AdminProductLog extends BaseLogEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "action", nullable = false)
    private String action;

    @Builder
    public AdminProductLog(Admin admin, Product product, String action) {
        this.admin = admin;
        this.product = product;
        this.action = action;
    }
}