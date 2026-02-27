package store.onuljang.shop.admin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AdminDeliveryConfigRequest(
    @NotNull Boolean enabled,
    @NotNull @DecimalMin("0") BigDecimal minAmount,
    @NotNull @DecimalMin("0") BigDecimal feeNear,
    @NotNull @DecimalMin("0") BigDecimal feePer100m,
    @NotNull @DecimalMin("0") Double feeDistanceKm,
    @NotNull @DecimalMin("0") Double maxDistanceKm,
    @NotNull @Min(0) @Max(27) Integer startHour,
    @NotNull @Min(0) @Max(59) Integer startMinute,
    @NotNull @Min(0) @Max(27) Integer endHour,
    @NotNull @Min(0) @Max(59) Integer endMinute
) {}
