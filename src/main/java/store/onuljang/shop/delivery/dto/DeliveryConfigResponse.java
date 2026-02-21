package store.onuljang.shop.delivery.dto;

import lombok.Builder;
import store.onuljang.shop.delivery.config.DeliveryConfigSnapshot;

import java.math.BigDecimal;

@Builder
public record DeliveryConfigResponse(
    boolean enabled,
    double storeLat,
    double storeLng,
    double maxDistanceKm,
    double feeDistanceKm,
    BigDecimal minAmount,
    BigDecimal feeNear,
    BigDecimal feePer100m,
    int startHour,
    int startMinute,
    int endHour,
    int endMinute
) {
    public static DeliveryConfigResponse from(DeliveryConfigSnapshot config) {
        return DeliveryConfigResponse.builder()
            .enabled(config.enabled())
            .storeLat(config.storeLat())
            .storeLng(config.storeLng())
            .maxDistanceKm(config.maxDistanceKm())
            .feeDistanceKm(config.feeDistanceKm())
            .minAmount(config.minAmount())
            .feeNear(config.feeNear())
            .feePer100m(config.feePer100m())
            .startHour(config.startHour())
            .startMinute(config.startMinute())
            .endHour(config.endHour())
            .endMinute(config.endMinute())
            .build();
    }
}
