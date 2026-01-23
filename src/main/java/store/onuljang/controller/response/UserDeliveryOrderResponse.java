package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.repository.entity.enums.DeliveryStatus;

import java.math.BigDecimal;

@Builder
public record UserDeliveryOrderResponse(
    long reservationId,
    long orderId,
    DeliveryStatus status,
    BigDecimal deliveryFee,
    int deliveryHour,
    int deliveryMinute
) {
    public static UserDeliveryOrderResponse from(DeliveryOrder order, store.onuljang.repository.entity.Reservation reservation) {
        return UserDeliveryOrderResponse.builder()
            .reservationId(reservation.getId())
            .orderId(order.getId())
            .status(order.getStatus())
            .deliveryFee(order.getDeliveryFee())
            .deliveryHour(order.getDeliveryHour())
            .deliveryMinute(order.getDeliveryMinute())
            .build();
    }
}
