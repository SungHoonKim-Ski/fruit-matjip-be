package store.onuljang.controller.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdminDeliveryAcceptRequest(
    @NotNull @Min(10) @Max(120) Integer estimatedMinutes
) {}
