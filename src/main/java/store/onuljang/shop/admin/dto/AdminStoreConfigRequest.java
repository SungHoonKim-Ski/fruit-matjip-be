package store.onuljang.shop.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdminStoreConfigRequest(
    @NotNull @Min(0) @Max(27) Integer reservationDeadlineHour,
    @NotNull @Min(0) @Max(59) Integer reservationDeadlineMinute,
    @NotNull @Min(0) @Max(27) Integer cancellationDeadlineHour,
    @NotNull @Min(0) @Max(59) Integer cancellationDeadlineMinute,
    @NotNull @Min(0) @Max(27) Integer pickupDeadlineHour,
    @NotNull @Min(0) @Max(59) Integer pickupDeadlineMinute
) {}
