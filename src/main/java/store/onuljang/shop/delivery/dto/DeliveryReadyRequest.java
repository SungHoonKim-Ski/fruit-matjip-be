package store.onuljang.shop.delivery.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

public record DeliveryReadyRequest(
    @NotEmpty @UniqueElements List<@NotNull @NotBlank String> reservationCodes,
    @NotNull @Min(0) @Max(23) Integer deliveryHour,
    @NotNull @Min(0) @Max(59) Integer deliveryMinute,
    @NotBlank String phone,
    @NotBlank String postalCode,
    @NotBlank String address1,
    String address2,
    @NotNull Double latitude,
    @NotNull Double longitude,
    @Min(0) @Max(23) Integer scheduledDeliveryHour,
    @Min(0) @Max(59) Integer scheduledDeliveryMinute,
    @NotBlank String idempotencyKey
) {}
