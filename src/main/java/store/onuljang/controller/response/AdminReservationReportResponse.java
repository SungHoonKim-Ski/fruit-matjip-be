package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.ReservationAll;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record AdminReservationReportResponse(
    List<AdminReservationResponse> response
) {
    @Builder
    public record AdminReservationResponse(
        LocalDate pickupDate,
        String productName,
        String userName,
        int price,
        long quantity,
        BigDecimal amount) {

        public static AdminReservationResponse from(ReservationAll entity) {
            return AdminReservationResponse.builder()
                .pickupDate(entity.getPickupDate())
                .productName(entity.getReservationProductName())
                .userName(entity.getReservationUserName())
                .price(entity.getQuantity())
                .quantity(entity.getQuantity())
                .amount(entity.getAmount())
            .build();
        }
    }

    public static AdminReservationReportResponse from(List<ReservationAll> entities) {
        return AdminReservationReportResponse.builder()
            .response(entities.stream()
                .map(AdminReservationResponse::from).toList())
            .build();
    }
}