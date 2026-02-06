package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.enums.DeliveryStatus;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record AdminDeliveryListResponse(
    List<AdminDeliveryResponse> response
) {
    @Builder
    public record AdminDeliveryResponse(
        long id,
        String buyerName,
        List<ReservationItem> reservationItems,
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
            return AdminDeliveryResponse.builder()
                .id(order.getId())
                .buyerName(order.getUser().getName())
                .reservationItems(reservations.stream()
                    .map(reservation -> ReservationItem.builder()
                        .id(reservation.getId())
                        .productName(reservation.getReservationProductName())
                        .quantity(reservation.getQuantity())
                        .build())
                    .toList())
                .totalAmount(totalAmount)
                .status(order.getStatus())
                .phone(order.getPhone())
                .postalCode(order.getPostalCode())
                .address1(order.getAddress1())
                .address2(order.getAddress2())
                .build();
        }
    }

    @Builder
    public record ReservationItem(
        long id,
        String productName,
        int quantity
    ) {}

    public static AdminDeliveryListResponse from(List<DeliveryOrder> orders) {
        return AdminDeliveryListResponse.builder()
            .response(orders.stream().map(AdminDeliveryResponse::from).toList())
            .build();
    }
}
