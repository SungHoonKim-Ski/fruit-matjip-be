package store.onuljang.controller.request;

import jakarta.validation.constraints.NotNull;

public record DeliveryFeeRequest(
    @NotNull Double latitude,
    @NotNull Double longitude
) {}
