package store.onuljang.config;

import store.onuljang.repository.entity.DeliveryConfig;

import java.math.BigDecimal;

public record DeliveryConfigSnapshot(
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
    public static DeliveryConfigSnapshot from(DeliveryConfigDto base, DeliveryConfig override) {
        if (override == null) {
            return new DeliveryConfigSnapshot(
                true,
                base.getStoreLat(),
                base.getStoreLng(),
                base.getMaxDistanceKm(),
                base.getFeeDistanceKm(),
                base.getMinAmount(),
                base.getFeeNear(),
                base.getFeePer100m(),
                base.getStartHour(),
                base.getStartMinute(),
                base.getEndHour(),
                base.getEndMinute()
            );
        }
        return new DeliveryConfigSnapshot(
            override.isEnabled(),
            base.getStoreLat(),
            base.getStoreLng(),
            override.getMaxDistanceKm(),
            override.getFeeDistanceKm(),
            override.getMinAmount(),
            override.getFeeNear(),
            override.getFeePer100m(),
            override.getStartHour(),
            override.getStartMinute(),
            override.getEndHour(),
            override.getEndMinute()
        );
    }
}
