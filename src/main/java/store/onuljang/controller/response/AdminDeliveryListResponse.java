package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.enums.DeliveryStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        Integer estimatedMinutes,
        LocalDateTime acceptedAt,
        String phone,
        String postalCode,
        String address1,
        String address2,
        BigDecimal distanceKm,
        BigDecimal deliveryFee
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
                        .amount(reservation.getAmount())
                        .build())
                    .toList())
                .totalAmount(totalAmount)
                .status(order.getStatus())
                .estimatedMinutes(order.getEstimatedMinutes())
                .acceptedAt(order.getAcceptedAt())
                .phone(order.getPhone())
                .postalCode(order.getPostalCode())
                .address1(order.getAddress1())
                .address2(order.getAddress2())
                .distanceKm(order.getDistanceKm())
                .deliveryFee(order.getDeliveryFee())
                .build();
        }
    }

    @Builder
    public record ReservationItem(
        long id,
        String productName,
        int quantity,
        BigDecimal amount
    ) {}

    public static AdminDeliveryListResponse from(List<DeliveryOrder> orders) {
        return AdminDeliveryListResponse.builder()
            .response(orders.stream().map(AdminDeliveryResponse::from).toList())
            .build();
    }
}
