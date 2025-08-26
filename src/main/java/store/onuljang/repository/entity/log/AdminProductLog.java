package store.onuljang.repository.entity.log;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import store.onuljang.log.admin.AdminLogEvent;
import store.onuljang.repository.entity.base.BaseLogEntity;
import store.onuljang.repository.entity.enums.AdminProductAction;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "admin_product_logs")
@Builder
public class AdminProductLog extends BaseLogEntity {

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "action", nullable = false)
    @Enumerated(EnumType.STRING)
    private AdminProductAction action;

    @Column(name = "quantity")
    private Integer quantity;
}