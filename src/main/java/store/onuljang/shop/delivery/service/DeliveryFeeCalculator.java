package store.onuljang.shop.delivery.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shop.delivery.config.DeliveryConfigSnapshot;
import store.onuljang.shared.exception.UserValidateException;
import store.onuljang.shop.delivery.util.MathUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryFeeCalculator {
    DeliveryConfigService deliveryConfigService;

    public FeeResult calculate(double targetLat, double targetLng) {
        DeliveryConfigSnapshot config = deliveryConfigService.getConfig();
        double distanceKmValue = MathUtil.calculateDistanceKm(
            config.storeLat(),
            config.storeLng(),
            targetLat,
            targetLng
        );
        if (distanceKmValue > config.maxDistanceKm()) {
            throw new UserValidateException("배달 가능 거리(" + MathUtil.trimDistance(config.maxDistanceKm())
                + "km)를 초과했습니다.");
        }

        BigDecimal distanceKm = BigDecimal.valueOf(distanceKmValue).setScale(3, RoundingMode.HALF_UP);
        BigDecimal deliveryFee;
        double baseDistance = config.feeDistanceKm();
        if (distanceKmValue <= baseDistance) {
            deliveryFee = config.feeNear();
        } else {
            double extraKm = Math.max(0, distanceKmValue - baseDistance);
            long extraUnits = (long) Math.ceil(extraKm / 0.1d);
            deliveryFee = config.feeNear()
                .add(config.feePer100m().multiply(BigDecimal.valueOf(extraUnits)));
        }
        return new FeeResult(distanceKm, deliveryFee, distanceKmValue);
    }

    public record FeeResult(BigDecimal distanceKm, BigDecimal deliveryFee, double distanceKmValue) {}
}
