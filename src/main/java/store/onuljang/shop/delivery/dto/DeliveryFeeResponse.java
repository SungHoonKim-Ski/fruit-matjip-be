package store.onuljang.shop.delivery.dto;

import lombok.Builder;
import store.onuljang.shop.delivery.service.DeliveryFeeCalculator;

import java.math.BigDecimal;

@Builder
public record DeliveryFeeResponse(
    BigDecimal distanceKm,
    BigDecimal deliveryFee
) {
    public static DeliveryFeeResponse from(DeliveryFeeCalculator.FeeResult result) {
        return DeliveryFeeResponse.builder()
            .distanceKm(result.distanceKm())
            .deliveryFee(result.deliveryFee())
            .build();
    }
}
