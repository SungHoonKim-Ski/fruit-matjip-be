package store.onuljang.shop.delivery.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shop.delivery.config.DeliveryConfigDto;
import store.onuljang.shop.delivery.config.DeliveryConfigSnapshot;
import store.onuljang.shop.admin.dto.AdminDeliveryConfigRequest;
import store.onuljang.shared.exception.AdminValidateException;
import store.onuljang.shop.delivery.repository.DeliveryConfigRepository;
import store.onuljang.shop.delivery.entity.DeliveryConfig;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryConfigService {
    DeliveryConfigDto baseConfig;
    DeliveryConfigRepository deliveryConfigRepository;

    public DeliveryConfigSnapshot getConfig() {
        return DeliveryConfigSnapshot.from(baseConfig, deliveryConfigRepository.findTopByOrderByIdAsc().orElse(null));
    }

    @Transactional
    public DeliveryConfigSnapshot update(AdminDeliveryConfigRequest request) {
        if (request.feeDistanceKm() > request.maxDistanceKm()) {
            throw new AdminValidateException("배달 기준 거리보다 최대 거리가 짧습니다.");
        }
        int startTotal = request.startHour() * 60 + request.startMinute();
        int endTotal = request.endHour() * 60 + request.endMinute();
        if (startTotal >= endTotal) {
            throw new AdminValidateException("배달 시작 시간은 종료 시간보다 빨라야 합니다.");
        }

        DeliveryConfig config = deliveryConfigRepository.findTopByOrderByIdAsc()
            .orElseGet(() -> DeliveryConfig.builder()
                .enabled(true)
                .maxDistanceKm(baseConfig.getMaxDistanceKm())
                .feeDistanceKm(baseConfig.getFeeDistanceKm())
                .minAmount(baseConfig.getMinAmount())
                .feeNear(baseConfig.getFeeNear())
                .feePer100m(baseConfig.getFeePer100m())
                .startHour(baseConfig.getStartHour())
                .startMinute(baseConfig.getStartMinute())
                .endHour(baseConfig.getEndHour())
                .endMinute(baseConfig.getEndMinute())
                .build());

        config.update(
            request.enabled(),
            request.maxDistanceKm(),
            request.feeDistanceKm(),
            request.minAmount(),
            request.feeNear(),
            request.feePer100m(),
            request.startHour(),
            request.startMinute(),
            request.endHour(),
            request.endMinute()
        );

        deliveryConfigRepository.save(config);
        return DeliveryConfigSnapshot.from(baseConfig, config);
    }
}
