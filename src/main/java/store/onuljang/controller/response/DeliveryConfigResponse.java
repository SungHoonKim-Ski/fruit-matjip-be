package store.onuljang.controller.response;

import lombok.Builder;
import store.onuljang.config.DeliveryConfigDto;

import java.math.BigDecimal;

@Builder
public record DeliveryConfigResponse(
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
    public static DeliveryConfigResponse from(DeliveryConfigDto config) {
        return DeliveryConfigResponse.builder()
            .storeLat(config.getStoreLat())
            .storeLng(config.getStoreLng())
            .maxDistanceKm(config.getMaxDistanceKm())
            .feeDistanceKm(config.getFeeDistanceKm())
            .minAmount(config.getMinAmount())
            .feeNear(config.getFeeNear())
            .feePer100m(config.getFeePer100m())
            .startHour(config.getStartHour())
            .startMinute(config.getStartMinute())
            .endHour(config.getEndHour())
            .endMinute(config.getEndMinute())
            .build();
    }
}
