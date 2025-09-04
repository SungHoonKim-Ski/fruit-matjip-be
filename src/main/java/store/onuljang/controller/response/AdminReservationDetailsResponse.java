package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.repository.entity.ProductDailyAgg;
import store.onuljang.repository.entity.ReservationAll;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record AdminReservationDetailsResponse(
    List<AdminReservationDetailResponse> response
) {
    @Builder
    public record AdminReservationDetailResponse(String productName, int quantity, BigDecimal amount) {

        public static AdminReservationDetailResponse from(ProductDailyAgg entity) {
            return AdminReservationDetailResponse.builder()
                .productName(entity.getProductName())
                .amount(entity.getAmount())
                .quantity(entity.getQuantity())
            .build();
        }
    }

    public static AdminReservationDetailsResponse from(List<ProductDailyAgg> entities) {
        return AdminReservationDetailsResponse.builder()
            .response(entities.stream().map(AdminReservationDetailResponse::from).toList())
            .build();
    }
}