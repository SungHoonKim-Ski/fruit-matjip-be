package store.onuljang.courier.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import store.onuljang.courier.entity.CourierClaim;
import store.onuljang.shared.entity.enums.CourierClaimStatus;
import store.onuljang.shared.entity.enums.CourierClaimType;
import store.onuljang.shared.entity.enums.ShippingFeeBearer;

public record CourierClaimResponse(
        Long id,
        Long orderId,
        String orderDisplayCode,
        Long orderItemId,
        String productName,
        CourierClaimType claimType,
        CourierClaimStatus claimStatus,
        String reason,
        String adminNote,
        BigDecimal refundAmount,
        Long reshipOrderId,
        ShippingFeeBearer returnShippingFeeBearer,
        LocalDateTime resolvedAt,
        LocalDateTime createdAt) {

    public static CourierClaimResponse from(CourierClaim claim) {
        return new CourierClaimResponse(
                claim.getId(),
                claim.getCourierOrder() != null ? claim.getCourierOrder().getId() : null,
                claim.getCourierOrder() != null ? claim.getCourierOrder().getDisplayCode() : null,
                claim.getCourierOrderItem() != null ? claim.getCourierOrderItem().getId() : null,
                claim.getCourierOrderItem() != null
                        ? claim.getCourierOrderItem().getProductName()
                        : null,
                claim.getClaimType(),
                claim.getClaimStatus(),
                claim.getReason(),
                claim.getAdminNote(),
                claim.getRefundAmount(),
                claim.getReshipOrderId(),
                claim.getReturnShippingFeeBearer(),
                claim.getResolvedAt(),
                claim.getCreatedAt());
    }
}
