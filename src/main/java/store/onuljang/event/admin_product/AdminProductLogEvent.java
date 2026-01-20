package store.onuljang.event.admin_product;

import lombok.Builder;
import store.onuljang.repository.entity.enums.AdminProductAction;
import store.onuljang.repository.entity.log.AdminProductLog;

@Builder
public record AdminProductLogEvent(
    Long adminId,
    Long productId,
    Integer quantity,
    AdminProductAction action
)
{
    public static AdminProductLog from(AdminProductLogEvent event) {
        return AdminProductLog.builder()
            .adminId(event.adminId)
            .productId(event.productId())
            .quantity(event.quantity())
            .action(event.action())
            .build();
    }
}
