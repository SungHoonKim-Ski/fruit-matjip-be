package store.onuljang.shop.product.event;

import lombok.Builder;
import store.onuljang.shared.entity.enums.AdminProductAction;
import store.onuljang.shop.admin.entity.log.AdminProductLog;

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
