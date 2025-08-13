package store.onuljang.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ReservationRequest(
    @PositiveOrZero @NotNull Long productId,
    @Positive @NotNull Integer quantity,
    @Positive @NotNull BigDecimal amount
) {

}
