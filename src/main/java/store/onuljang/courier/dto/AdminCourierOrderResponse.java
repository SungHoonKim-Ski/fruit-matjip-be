package store.onuljang.courier.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.shared.entity.enums.CourierOrderStatus;

public record AdminCourierOrderResponse(
        Long id,
        String displayCode,
        CourierOrderStatus status,
        String receiverName,
        String receiverPhone,
        String productSummary,
        int totalQuantity,
        BigDecimal totalAmount,
        String waybillNumber,
        LocalDateTime paidAt,
        LocalDateTime shippedAt,
        LocalDateTime createdAt) {

    public static AdminCourierOrderResponse from(CourierOrder order) {
        return new AdminCourierOrderResponse(
                order.getId(),
                order.getDisplayCode(),
                order.getStatus(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getProductSummary(),
                order.getTotalQuantity(),
                order.getTotalAmount(),
                order.getWaybillNumber(),
                order.getPaidAt(),
                order.getShippedAt(),
                order.getCreatedAt());
    }
}
