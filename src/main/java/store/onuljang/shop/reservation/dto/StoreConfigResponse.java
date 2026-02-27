package store.onuljang.shop.reservation.dto;

import lombok.Builder;
import store.onuljang.shop.reservation.config.StoreConfigSnapshot;

@Builder
public record StoreConfigResponse(
    int reservationDeadlineHour,
    int reservationDeadlineMinute,
    int cancellationDeadlineHour,
    int cancellationDeadlineMinute,
    int pickupDeadlineHour,
    int pickupDeadlineMinute
) {
    public static StoreConfigResponse from(StoreConfigSnapshot config) {
        return StoreConfigResponse.builder()
            .reservationDeadlineHour(config.reservationDeadlineHour())
            .reservationDeadlineMinute(config.reservationDeadlineMinute())
            .cancellationDeadlineHour(config.cancellationDeadlineHour())
            .cancellationDeadlineMinute(config.cancellationDeadlineMinute())
            .pickupDeadlineHour(config.pickupDeadlineHour())
            .pickupDeadlineMinute(config.pickupDeadlineMinute())
            .build();
    }
}
