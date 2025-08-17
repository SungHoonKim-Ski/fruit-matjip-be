package store.onuljang.log.admin_product;

import lombok.Builder;
import store.onuljang.repository.entity.enums.AdminProductAction;
import store.onuljang.repository.entity.log.AdminProductLog;

@Builder
public record AdminProductLogEvent(
    Long adminId,
    Long productId,
    AdminProductAction action
)
{
    public static AdminProductLog from(AdminProductLogEvent event) {
        return AdminProductLog.builder()
            .adminId(event.adminId)
            .productId(event.productId())
            .action(event.action())
            .build();
    }
}