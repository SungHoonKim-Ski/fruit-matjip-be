package store.onuljang.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeliveryInfoRequest(
    @NotBlank String phone,
    @NotBlank String postalCode,
    @NotBlank String address1,
    String address2
) {}
