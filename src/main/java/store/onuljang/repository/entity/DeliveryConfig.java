package store.onuljang.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import store.onuljang.repository.entity.base.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "delivery_config")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeliveryConfig extends BaseEntity {

    @Column(name = "enabled", nullable = false)
    boolean enabled;

    @Column(name = "max_distance_km", nullable = false)
    double maxDistanceKm;

    @Column(name = "fee_distance_km", nullable = false)
    double feeDistanceKm;

    @Column(name = "min_amount", nullable = false, precision = 12, scale = 2)
    BigDecimal minAmount;

    @Column(name = "fee_near", nullable = false, precision = 12, scale = 2)
    BigDecimal feeNear;

    @Column(name = "fee_per_100m", nullable = false, precision = 12, scale = 2)
    BigDecimal feePer100m;

    @Column(name = "start_hour", nullable = false)
    int startHour;

    @Column(name = "start_minute", nullable = false)
    int startMinute;

    @Column(name = "end_hour", nullable = false)
    int endHour;

    @Column(name = "end_minute", nullable = false)
    int endMinute;

    @Builder
    public DeliveryConfig(boolean enabled, double maxDistanceKm, double feeDistanceKm, BigDecimal minAmount,
            BigDecimal feeNear, BigDecimal feePer100m, int startHour, int startMinute, int endHour, int endMinute) {
        this.enabled = enabled;
        this.maxDistanceKm = maxDistanceKm;
        this.feeDistanceKm = feeDistanceKm;
        this.minAmount = minAmount;
        this.feeNear = feeNear;
        this.feePer100m = feePer100m;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }

    public void update(boolean enabled, double maxDistanceKm, double feeDistanceKm, BigDecimal minAmount,
            BigDecimal feeNear, BigDecimal feePer100m, int startHour, int startMinute, int endHour, int endMinute) {
        this.enabled = enabled;
        this.maxDistanceKm = maxDistanceKm;
        this.feeDistanceKm = feeDistanceKm;
        this.minAmount = minAmount;
        this.feeNear = feeNear;
        this.feePer100m = feePer100m;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }
}
