package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.config.DeliveryConfigDto;
import store.onuljang.exception.UserValidateException;
import store.onuljang.util.MathUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryFeeCalculator {
    DeliveryConfigDto deliveryConfigDto;

    public FeeResult calculate(double targetLat, double targetLng) {
        double distanceKmValue = MathUtil.calculateDistanceKm(
            deliveryConfigDto.getStoreLat(),
            deliveryConfigDto.getStoreLng(),
            targetLat,
            targetLng
        );
        if (distanceKmValue > deliveryConfigDto.getMaxDistanceKm()) {
            throw new UserValidateException("배달 가능 거리(" + MathUtil.trimDistance(deliveryConfigDto.getMaxDistanceKm())
                + "km)를 초과했습니다.");
        }

        BigDecimal distanceKm = BigDecimal.valueOf(distanceKmValue).setScale(3, RoundingMode.HALF_UP);
        BigDecimal deliveryFee;
        double baseDistance = deliveryConfigDto.getFeeDistanceKm();
        if (distanceKmValue <= baseDistance) {
            deliveryFee = deliveryConfigDto.getFeeNear();
        } else {
            double extraKm = Math.max(0, distanceKmValue - baseDistance);
            long extraUnits = (long) Math.ceil(extraKm / 0.1d);
            deliveryFee = deliveryConfigDto.getFeeNear()
                .add(deliveryConfigDto.getFeePer100m().multiply(BigDecimal.valueOf(extraUnits)));
        }
        return new FeeResult(distanceKm, deliveryFee, distanceKmValue);
    }

    public record FeeResult(BigDecimal distanceKm, BigDecimal deliveryFee, double distanceKmValue) {}
}
