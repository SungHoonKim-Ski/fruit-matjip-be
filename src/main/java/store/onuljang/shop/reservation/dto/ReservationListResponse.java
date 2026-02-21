package store.onuljang.shop.reservation.dto;

import lombok.Builder;
import store.onuljang.shop.reservation.entity.Reservation;
import store.onuljang.shared.entity.enums.ReservationStatus;
import store.onuljang.shared.user.dto.UserDeliveryOrderResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record ReservationListResponse(
    List<ReservationResponse> response
) {
    @Builder
    public record ReservationResponse(
        String displayCode,
        BigDecimal amount,
        String productImage,
        String productName,
        LocalDate orderDate,
        int quantity,
        boolean selfPick,
        boolean deliveryAvailable,
        ReservationStatus status,
        UserDeliveryOrderResponse delivery) {

        public static ReservationResponse from(Reservation entity) {
            return ReservationResponse.builder()
                .displayCode(entity.getDisplayCode())
                .amount(entity.getAmount())
                .productImage(entity.getReservationProductUrl())
                .productName(entity.getReservationProductName())
                .orderDate(entity.getPickupDate())
                .quantity(entity.getQuantity())
                .selfPick(entity.getSelfPick())
                .deliveryAvailable(entity.getDeliveryAvailable())
                .status(entity.getStatus())
                .delivery(UserDeliveryOrderResponse.from(entity))
            .build();
        }
    }

    public static ReservationListResponse from(List<Reservation> entities) {
        return ReservationListResponse.builder()
            .response(entities.stream().map(ReservationResponse::from).toList())
            .build();
    }
}
