package store.onuljang.shop.admin.dto;

import lombok.Builder;
import store.onuljang.shop.delivery.entity.DeliveryOrder;
import store.onuljang.shop.reservation.entity.Reservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AdminDeliverySseResponse(
    long orderId,
    String displayCode,
    List<Long> reservationIds,
    int reservationCount,
    String buyerName,
    String productSummary,
    LocalDate deliveryDate,
    int deliveryHour,
    int deliveryMinute,
    Integer estimatedMinutes,
    LocalDateTime acceptedAt,
    List<ReservationItem> reservationItems,
    BigDecimal totalAmount,
    String phone,
    String address1,
    String address2,
    BigDecimal distanceKm,
    BigDecimal deliveryFee,
    LocalDateTime paidAt, // 영수증 출력용 결제 시각
    Integer scheduledDeliveryHour,
    Integer scheduledDeliveryMinute
) {
    @Builder
    public record ReservationItem(
        long id,
        String productName,
        int quantity,
        BigDecimal amount // 영수증 출력용 개별 상품 금액
    ) {}

    public static AdminDeliverySseResponse from(DeliveryOrder order) {
        List<Reservation> reservations = order.getReservations();
        return AdminDeliverySseResponse.builder()
            .orderId(order.getId())
            .displayCode(order.getDisplayCode())
            .reservationIds(order.getReservationIds())
            .reservationCount(reservations.size())
            .buyerName(order.getUser().getName())
            .productSummary(Reservation.buildFullSummary(reservations))
            .deliveryDate(order.getDeliveryDate())
            .deliveryHour(order.getDeliveryHour())
            .deliveryMinute(order.getDeliveryMinute())
            .estimatedMinutes(order.getEstimatedMinutes())
            .acceptedAt(order.getAcceptedAt())
            .reservationItems(reservations.stream()
                .map(r -> ReservationItem.builder()
                    .id(r.getId())
                    .productName(r.getReservationProductName())
                    .quantity(r.getQuantity())
                    .amount(r.getAmount())
                    .build())
                .toList())
            .totalAmount(order.getTotalAmount())
            .phone(order.getPhone())
            .address1(order.getAddress1())
            .address2(order.getAddress2())
            .distanceKm(order.getDistanceKm())
            .deliveryFee(order.getDeliveryFee())
            .paidAt(order.getPaidAt())
            .scheduledDeliveryHour(order.getScheduledDeliveryHour())
            .scheduledDeliveryMinute(order.getScheduledDeliveryMinute())
            .build();
    }
}
