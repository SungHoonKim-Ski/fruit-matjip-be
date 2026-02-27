package store.onuljang.shop.reservation.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shop.admin.dto.AdminStoreConfigRequest;
import store.onuljang.shop.reservation.config.StoreConfigDto;
import store.onuljang.shop.reservation.config.StoreConfigSnapshot;
import store.onuljang.shop.reservation.entity.StoreConfig;
import store.onuljang.shop.reservation.repository.StoreConfigRepository;
import store.onuljang.shared.exception.AdminValidateException;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreConfigService {
    StoreConfigDto baseConfig;
    StoreConfigRepository storeConfigRepository;

    public StoreConfigSnapshot getConfig() {
        return StoreConfigSnapshot.from(baseConfig, storeConfigRepository.findTopByOrderByIdAsc().orElse(null));
    }

    @Transactional
    public StoreConfigSnapshot update(AdminStoreConfigRequest request) {
        validateHour(request.reservationDeadlineHour(), "예약 마감 시");
        validateHour(request.cancellationDeadlineHour(), "취소 마감 시");
        validateHour(request.pickupDeadlineHour(), "수령 마감 시");
        validateMinute(request.reservationDeadlineMinute(), "예약 마감 분");
        validateMinute(request.cancellationDeadlineMinute(), "취소 마감 분");
        validateMinute(request.pickupDeadlineMinute(), "수령 마감 분");

        int cancellationTotal = request.cancellationDeadlineHour() * 60 + request.cancellationDeadlineMinute();
        int reservationTotal = request.reservationDeadlineHour() * 60 + request.reservationDeadlineMinute();
        int pickupTotal = request.pickupDeadlineHour() * 60 + request.pickupDeadlineMinute();

        if (cancellationTotal > reservationTotal) {
            throw new AdminValidateException("취소 마감 시간은 예약 마감 시간보다 늦을 수 없습니다.");
        }
        if (reservationTotal > pickupTotal) {
            throw new AdminValidateException("예약 마감 시간은 수령 마감 시간보다 늦을 수 없습니다.");
        }

        StoreConfig config = storeConfigRepository.findTopByOrderByIdAsc()
            .orElseGet(() -> StoreConfig.builder()
                .reservationDeadlineHour(baseConfig.getReservationDeadlineHour())
                .reservationDeadlineMinute(baseConfig.getReservationDeadlineMinute())
                .cancellationDeadlineHour(baseConfig.getCancellationDeadlineHour())
                .cancellationDeadlineMinute(baseConfig.getCancellationDeadlineMinute())
                .pickupDeadlineHour(baseConfig.getPickupDeadlineHour())
                .pickupDeadlineMinute(baseConfig.getPickupDeadlineMinute())
                .build());

        config.update(
            request.reservationDeadlineHour(),
            request.reservationDeadlineMinute(),
            request.cancellationDeadlineHour(),
            request.cancellationDeadlineMinute(),
            request.pickupDeadlineHour(),
            request.pickupDeadlineMinute()
        );

        storeConfigRepository.save(config);
        return StoreConfigSnapshot.from(baseConfig, config);
    }

    private void validateHour(int hour, String label) {
        if (hour < 0 || hour > 27) {
            throw new AdminValidateException(label + " 값은 0~27 사이여야 합니다.");
        }
    }

    private void validateMinute(int minute, String label) {
        if (minute < 0 || minute > 59) {
            throw new AdminValidateException(label + " 값은 0~59 사이여야 합니다.");
        }
    }
}
