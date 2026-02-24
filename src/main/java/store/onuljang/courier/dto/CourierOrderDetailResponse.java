package store.onuljang.courier.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.shared.entity.enums.CourierCompany;
import store.onuljang.shared.entity.enums.CourierOrderStatus;

public record CourierOrderDetailResponse(
        String displayCode,
        CourierOrderStatus status,
        String statusDisplayName,
        String receiverName,
        String receiverPhone,
        String postalCode,
        String address1,
        String address2,
        String shippingMemo,
        Boolean isIsland,
        BigDecimal productAmount,
        BigDecimal shippingFee,
        BigDecimal islandSurcharge,
        BigDecimal totalAmount,
        BigDecimal pointUsed,
        BigDecimal pgPaymentAmount,
        String waybillNumber,
        CourierCompany courierCompany,
        LocalDateTime paidAt,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt,
        List<CourierOrderItemResponse> items) {

    public static CourierOrderDetailResponse from(CourierOrder order) {
        List<CourierOrderItemResponse> itemResponses =
                order.getItems().stream().map(CourierOrderItemResponse::from).toList();
        return new CourierOrderDetailResponse(
                order.getDisplayCode(),
                order.getStatus(),
                order.getStatus().getCustomerDisplayName(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getPostalCode(),
                order.getAddress1(),
                order.getAddress2(),
                order.getShippingMemo(),
                order.getIsIsland(),
                order.getProductAmount(),
                order.getShippingFee(),
                order.getIslandSurcharge(),
                order.getTotalAmount(),
                order.getPointUsed(),
                order.getPgPaymentAmount(),
                order.getWaybillNumber(),
                order.getCourierCompany(),
                order.getPaidAt(),
                order.getShippedAt(),
                order.getDeliveredAt(),
                order.getCreatedAt(),
                itemResponses);
    }
}
