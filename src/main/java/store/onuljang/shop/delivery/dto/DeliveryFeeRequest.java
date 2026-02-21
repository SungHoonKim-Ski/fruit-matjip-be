package store.onuljang.shop.delivery.dto;

import jakarta.validation.constraints.NotNull;

public record DeliveryFeeRequest(
    @NotNull Double latitude,
    @NotNull Double longitude
) {}
