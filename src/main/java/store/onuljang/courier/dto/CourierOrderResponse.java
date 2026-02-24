package store.onuljang.courier.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.shared.entity.enums.CourierOrderStatus;

public record CourierOrderResponse(
        String displayCode,
        CourierOrderStatus status,
        String statusDisplayName,
        String productSummary,
        int totalQuantity,
        BigDecimal totalAmount,
        BigDecimal pointUsed,
        BigDecimal pgPaymentAmount,
        String thumbnailUrl,
        LocalDateTime createdAt) {

    public static CourierOrderResponse from(CourierOrder order) {
        String thumbnail =
                (order.getItems() != null && !order.getItems().isEmpty()
                                && order.getItems().get(0).getCourierProduct() != null)
                        ? order.getItems().get(0).getCourierProduct().getProductUrl()
                        : null;
        return new CourierOrderResponse(
                order.getDisplayCode(),
                order.getStatus(),
                order.getStatus().getCustomerDisplayName(),
                order.getProductSummary(),
                order.getTotalQuantity(),
                order.getTotalAmount(),
                order.getPointUsed(),
                order.getPgPaymentAmount(),
                thumbnail,
                order.getCreatedAt());
    }
}
