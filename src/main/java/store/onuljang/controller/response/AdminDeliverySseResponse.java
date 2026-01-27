package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.repository.entity.Reservation;

import java.time.LocalDate;
import java.util.List;

@Builder
public record AdminDeliverySseResponse(
    long orderId,
    List<Long> reservationIds,
    int reservationCount,
    String buyerName,
    String productSummary,
    LocalDate deliveryDate,
    int deliveryHour,
    int deliveryMinute
) {
    public static AdminDeliverySseResponse from(DeliveryOrder order) {
        List<Reservation> reservations = order.getReservations();
        return AdminDeliverySseResponse.builder()
            .orderId(order.getId())
            .reservationIds(order.getReservationIds())
            .reservationCount(reservations.size())
            .buyerName(order.getUser().getName())
            .productSummary(Reservation.buildSummary(reservations))
            .deliveryDate(order.getDeliveryDate())
            .deliveryHour(order.getDeliveryHour())
            .deliveryMinute(order.getDeliveryMinute())
            .build();
    }
}
