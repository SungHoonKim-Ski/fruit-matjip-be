package store.onuljang.courier.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record CourierOrderReadyRequest(
        @NotEmpty @Valid List<OrderItemRequest> items,
        @NotBlank String receiverName,
        @NotBlank String receiverPhone,
        @NotBlank String postalCode,
        @NotBlank String address1,
        String address2,
        String shippingMemo,
        @NotBlank String pgProvider,
        @NotBlank String idempotencyKey,
        BigDecimal pointUsed) {

    public record OrderItemRequest(
            @NotNull @Positive Long courierProductId,
            @NotNull @Min(1) Integer quantity,
            List<Long> selectedOptionIds) {}
}
