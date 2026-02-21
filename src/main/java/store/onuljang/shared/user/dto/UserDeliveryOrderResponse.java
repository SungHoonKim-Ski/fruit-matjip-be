package store.onuljang.shared.user.dto;

import lombok.Builder;
import store.onuljang.shop.delivery.entity.DeliveryOrder;
import store.onuljang.shop.reservation.entity.Reservation;
import store.onuljang.shared.entity.enums.DeliveryStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record UserDeliveryOrderResponse(
    String displayCode,
    DeliveryStatus status,
    BigDecimal deliveryFee,
    int deliveryHour,
    int deliveryMinute,
    Integer estimatedMinutes,
    LocalDateTime acceptedAt,
    Integer scheduledDeliveryHour,
    Integer scheduledDeliveryMinute
) {
    public static UserDeliveryOrderResponse from(DeliveryOrder order, Reservation reservation) {
        return UserDeliveryOrderResponse.builder()
            .displayCode(order.getDisplayCode())
            .status(order.getStatus())
            .deliveryFee(order.getDeliveryFee())
            .deliveryHour(order.getDeliveryHour())
            .deliveryMinute(order.getDeliveryMinute())
            .estimatedMinutes(order.getEstimatedMinutes())
            .acceptedAt(order.getAcceptedAt())
            .scheduledDeliveryHour(order.getScheduledDeliveryHour())
            .scheduledDeliveryMinute(order.getScheduledDeliveryMinute())
            .build();
    }

    public static UserDeliveryOrderResponse from(Reservation reservation) {
        DeliveryOrder order = reservation.getDeliveryOrder();
        if (order == null) {
            return null;
        }
        if (order.getStatus() == DeliveryStatus.PENDING_PAYMENT
            || order.getStatus() == DeliveryStatus.CANCELED
            || order.getStatus() == DeliveryStatus.FAILED) {
            return null;
        }
        return from(order, reservation);
    }
}
