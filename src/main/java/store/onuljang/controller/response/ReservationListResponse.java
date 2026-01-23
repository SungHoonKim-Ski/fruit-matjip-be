package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record ReservationListResponse(
    List<ReservationResponse> response
) {
    @Builder
    public record ReservationResponse(
        long id,
        BigDecimal amount,
        String productImage,
        String productName,
        LocalDate orderDate,
        int quantity,
        boolean selfPick,
        boolean deliveryAvailable,
        ReservationStatus status,
        UserDeliveryOrderResponse delivery) {

        public static ReservationResponse from(Reservation entity, UserDeliveryOrderResponse delivery) {
            return ReservationResponse.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .productImage(entity.getReservationProductUrl())
                .productName(entity.getReservationProductName())
                .orderDate(entity.getPickupDate())
                .quantity(entity.getQuantity())
                .selfPick(entity.getSelfPick())
                .deliveryAvailable(entity.getDeliveryAvailable())
                .status(entity.getStatus())
                .delivery(delivery)
            .build();
        }
    }

    public static ReservationListResponse from(List<Reservation> entities, java.util.Map<Long, UserDeliveryOrderResponse> deliveryByReservationId) {
        return ReservationListResponse.builder()
            .response(entities.stream().map(entity -> ReservationResponse.from(entity, deliveryByReservationId.get(entity.getId()))).toList())
            .build();
    }
}
