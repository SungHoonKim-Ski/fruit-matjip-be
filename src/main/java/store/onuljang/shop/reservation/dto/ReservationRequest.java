package store.onuljang.shop.reservation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ReservationRequest(
    @PositiveOrZero @NotNull Long productId,
    @Positive @NotNull Integer quantity
) {

}
