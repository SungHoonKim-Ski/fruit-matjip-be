package store.onuljang.courier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import store.onuljang.shared.entity.enums.CourierClaimType;

public record CourierClaimRequest(
        @NotNull CourierClaimType claimType,
        Long courierOrderItemId,
        @NotBlank String reason) {}
