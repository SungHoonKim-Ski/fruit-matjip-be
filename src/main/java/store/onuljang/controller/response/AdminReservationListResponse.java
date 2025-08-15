package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record AdminReservationListResponse(
    List<AdminReservationResponse> response
) {
    @Builder
    public record AdminReservationResponse(
        long id,
        LocalDate orderDate,
        String productName,
        String userName,
        long quantity,
        BigDecimal amount,
        ReservationStatus status) {

        public static AdminReservationResponse from(Reservation entity) {
            return AdminReservationResponse.builder()
                .id(entity.getId())
                .orderDate(entity.getOrderDate())
                .productName(entity.getReservationProductName())
                .userName(entity.getReservationUserName())
                .quantity(entity.getQuantity())
                .amount(entity.getAmount())
                .status(entity.getStatus())
            .build();
        }
    }

    public static AdminReservationListResponse from(List<Reservation> entities) {
        return AdminReservationListResponse.builder()
            .response(entities.stream()
                .map(AdminReservationResponse::from).toList())
            .build();
    }
}