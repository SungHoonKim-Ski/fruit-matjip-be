package store.onuljang.courier.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CourierRecommendToggleRequest(
    @NotNull @Positive Long productId,
    Boolean recommended
) {}
