package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.ProductDailyAggRow;
import store.onuljang.repository.entity.ReservationAll;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record AdminReservationSummaryResponse(
    List<AdminSummaryResponse> summary
) {
    @Builder
    public record AdminSummaryResponse(LocalDate date, BigDecimal amount, int quantity) {
        public static AdminSummaryResponse from(ProductDailyAggRow entity) {
            return AdminSummaryResponse.builder()
                .date(entity.getSellDate())
                .amount(entity.getAmount())
                .quantity(entity.getQuantity())
            .build();
        }
    }

    public static AdminReservationSummaryResponse from(List<ProductDailyAggRow> entities) {
        return AdminReservationSummaryResponse.builder()
            .summary(entities.stream().map(AdminSummaryResponse::from).toList())
            .build();
    }
}