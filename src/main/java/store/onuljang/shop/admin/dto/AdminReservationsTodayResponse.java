package store.onuljang.shop.admin.dto;

import lombok.Builder;
import store.onuljang.shop.reservation.entity.ReservationSalesRow;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record AdminReservationsTodayResponse(
    List<AdminReservationTodayResponse> response
) {
    @Builder
    public record AdminReservationTodayResponse(long productId, String productName, int quantity, BigDecimal amount) {
        public static AdminReservationTodayResponse from(ReservationSalesRow salesRow) {
            return AdminReservationTodayResponse.builder()
                .productId(salesRow.getProductId())
                .productName(salesRow.getProductName())
                .amount(salesRow.getAmount())
                .quantity(salesRow.getQuantity())
                .build();
        }
    }

    public static AdminReservationsTodayResponse from(List<ReservationSalesRow> salesRows) {
        return AdminReservationsTodayResponse.builder()
            .response(salesRows.stream().map(AdminReservationTodayResponse::from).toList())
            .build();
    }
}
