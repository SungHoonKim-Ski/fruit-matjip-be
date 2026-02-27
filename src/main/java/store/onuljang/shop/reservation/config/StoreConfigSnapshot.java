package store.onuljang.shop.reservation.config;

import store.onuljang.shop.reservation.entity.StoreConfig;

public record StoreConfigSnapshot(
    int reservationDeadlineHour,
    int reservationDeadlineMinute,
    int cancellationDeadlineHour,
    int cancellationDeadlineMinute,
    int pickupDeadlineHour,
    int pickupDeadlineMinute
) {
    public static StoreConfigSnapshot from(StoreConfigDto base, StoreConfig override) {
        if (override == null) {
            return new StoreConfigSnapshot(
                base.getReservationDeadlineHour(),
                base.getReservationDeadlineMinute(),
                base.getCancellationDeadlineHour(),
                base.getCancellationDeadlineMinute(),
                base.getPickupDeadlineHour(),
                base.getPickupDeadlineMinute()
            );
        }
        return new StoreConfigSnapshot(
            override.getReservationDeadlineHour(),
            override.getReservationDeadlineMinute(),
            override.getCancellationDeadlineHour(),
            override.getCancellationDeadlineMinute(),
            override.getPickupDeadlineHour(),
            override.getPickupDeadlineMinute()
        );
    }
}
