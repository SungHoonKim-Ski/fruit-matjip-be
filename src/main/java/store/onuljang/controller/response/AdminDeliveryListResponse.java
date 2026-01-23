package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.DeliveryOrder;
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
        BigDecimal deliveryFee,
        BigDecimal totalAmount,
        DeliveryStatus status,
        String phone,
        String postalCode,
        String address1,
        String address2
    ) {
        public static AdminDeliveryResponse from(DeliveryOrder order) {
            List<store.onuljang.repository.entity.DeliveryOrderReservation> links = order.getDeliveryOrderReservations();
            List<store.onuljang.repository.entity.Reservation> reservations = links.stream()
                .map(store.onuljang.repository.entity.DeliveryOrderReservation::getReservation)
                .toList();
            BigDecimal totalAmount = reservations.stream()
                .map(store.onuljang.repository.entity.Reservation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(order.getDeliveryFee());
            int totalQuantity = reservations.stream().mapToInt(store.onuljang.repository.entity.Reservation::getQuantity).sum();
            String productSummary = buildSummary(reservations);
            return AdminDeliveryResponse.builder()
                .id(order.getId())
                .reservationIds(reservations.stream().map(store.onuljang.repository.entity.Reservation::getId).toList())
                .reservationCount(reservations.size())
                .buyerName(order.getUser().getName())
                .productSummary(productSummary)
                .totalQuantity(totalQuantity)
                .deliveryDate(order.getDeliveryDate())
                .deliveryHour(order.getDeliveryHour())
                .deliveryFee(order.getDeliveryFee())
                .totalAmount(totalAmount)
                .status(order.getStatus())
                .phone(order.getPhone())
                .postalCode(order.getPostalCode())
                .address1(order.getAddress1())
                .address2(order.getAddress2())
                .build();
        }

        private static String buildSummary(List<store.onuljang.repository.entity.Reservation> reservations) {
            if (reservations.isEmpty()) return "배달 주문";
            String first = reservations.get(0).getReservationProductName();
            if (reservations.size() == 1) return first;
            return first + " 외 " + (reservations.size() - 1) + "건";
        }
    }

    public static AdminDeliveryListResponse from(List<DeliveryOrder> orders) {
        return AdminDeliveryListResponse.builder()
            .response(orders.stream().map(AdminDeliveryResponse::from).toList())
            .build();
    }
}
