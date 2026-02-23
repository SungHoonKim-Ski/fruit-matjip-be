package store.onuljang.courier.dto;

import jakarta.validation.constraints.NotBlank;

public record CourierInfoRequest(
    @NotBlank String receiverName,
    @NotBlank String receiverPhone,
    @NotBlank String postalCode,
    @NotBlank String address1,
    String address2
) {}
