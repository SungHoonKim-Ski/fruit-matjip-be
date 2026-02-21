package store.onuljang.courier.dto;

import java.math.BigDecimal;
import store.onuljang.courier.entity.CourierOrderItem;
import store.onuljang.shared.entity.enums.CourierOrderItemStatus;

public record CourierOrderItemResponse(
        Long courierProductId,
        String productName,
        BigDecimal productPrice,
        int quantity,
        BigDecimal amount,
        String imageUrl,
        CourierOrderItemStatus itemStatus) {

    public static CourierOrderItemResponse from(CourierOrderItem item) {
        Long productId =
                (item.getCourierProduct() != null) ? item.getCourierProduct().getId() : null;
        String imageUrl =
                (item.getCourierProduct() != null) ? item.getCourierProduct().getProductUrl() : null;
        return new CourierOrderItemResponse(
                productId,
                item.getProductName(),
                item.getProductPrice(),
                item.getQuantity(),
                item.getAmount(),
                imageUrl,
                item.getItemStatus());
    }
}
