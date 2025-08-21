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
        ReservationStatus status) {

        public static ReservationResponse from(Reservation entity) {
            return ReservationResponse.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .productImage(entity.getReservationProductUrl())
                .productName(entity.getReservationProductName())
                .orderDate(entity.getPickupDate())
                .quantity(entity.getQuantity())
                .status(entity.getStatus())
            .build();
        }
    }

    public static ReservationListResponse from(List<Reservation> entities) {
        return ReservationListResponse.builder()
            .response(entities.stream()
                .map(ReservationResponse::from).toList())
            .build();
    }
}