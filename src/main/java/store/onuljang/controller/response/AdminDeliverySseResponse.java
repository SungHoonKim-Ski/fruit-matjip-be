package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.DeliveryOrder;

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
    int deliveryHour
) {
    public static AdminDeliverySseResponse from(DeliveryOrder order) {
        List<store.onuljang.repository.entity.Reservation> reservations = order.getDeliveryOrderReservations().stream()
            .map(store.onuljang.repository.entity.DeliveryOrderReservation::getReservation)
            .toList();
        return AdminDeliverySseResponse.builder()
            .orderId(order.getId())
            .reservationIds(reservations.stream().map(store.onuljang.repository.entity.Reservation::getId).toList())
            .reservationCount(reservations.size())
            .buyerName(order.getUser().getName())
            .productSummary(buildSummary(reservations))
            .deliveryDate(order.getDeliveryDate())
            .deliveryHour(order.getDeliveryHour())
            .build();
    }

    private static String buildSummary(List<store.onuljang.repository.entity.Reservation> reservations) {
        if (reservations.isEmpty()) return "배달 주문";
        String first = reservations.get(0).getReservationProductName();
        if (reservations.size() == 1) return first;
        return first + " 외 " + (reservations.size() - 1) + "건";
    }
}
