package store.onuljang.courier.dto;

import jakarta.validation.constraints.NotBlank;

public record CourierShipRequest(@NotBlank String waybillNumber) {}
