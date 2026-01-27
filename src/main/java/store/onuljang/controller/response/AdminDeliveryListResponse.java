package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.enums.DeliveryStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record AdminDeliveryListResponse(
    List<AdminDeliveryResponse> response
) {
    @Builder
    public record AdminDeliveryResponse(
        long id,
        List<Long> reservationIds,
        int reservationCount,
        String buyerName,
        String productSummary,
        int totalQuantity,
        LocalDate deliveryDate,
        int deliveryHour,
        int deliveryMinute,
        BigDecimal deliveryFee,
        BigDecimal totalAmount,
        DeliveryStatus status,
        String phone,
        String postalCode,
        String address1,
        String address2
    ) {
        public static AdminDeliveryResponse from(DeliveryOrder order) {
            List<Reservation> reservations = order.getReservations();
            BigDecimal totalAmount = order.getTotalAmount();
            int totalQuantity = order.getTotalQuantity();
            String productSummary = order.getProductSummary();
            return AdminDeliveryResponse.builder()
                .id(order.getId())
                .reservationIds(order.getReservationIds())
                .reservationCount(reservations.size())
                .buyerName(order.getUser().getName())
                .productSummary(productSummary)
                .totalQuantity(totalQuantity)
                .deliveryDate(order.getDeliveryDate())
                .deliveryHour(order.getDeliveryHour())
                .deliveryMinute(order.getDeliveryMinute())
                .deliveryFee(order.getDeliveryFee())
                .totalAmount(totalAmount)
                .status(order.getStatus())
                .phone(order.getPhone())
                .postalCode(order.getPostalCode())
                .address1(order.getAddress1())
                .address2(order.getAddress2())
                .build();
        }
    }

    public static AdminDeliveryListResponse from(List<DeliveryOrder> orders) {
        return AdminDeliveryListResponse.builder()
            .response(orders.stream().map(AdminDeliveryResponse::from).toList())
            .build();
    }
}
