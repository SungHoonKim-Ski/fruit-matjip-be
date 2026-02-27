package store.onuljang.shop.reservation.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class StoreConfigDto {
    @Value("${STORE.RESERVATION_DEADLINE_HOUR:19}")
    int reservationDeadlineHour;

    @Value("${STORE.RESERVATION_DEADLINE_MINUTE:30}")
    int reservationDeadlineMinute;

    @Value("${STORE.CANCELLATION_DEADLINE_HOUR:19}")
    int cancellationDeadlineHour;

    @Value("${STORE.CANCELLATION_DEADLINE_MINUTE:0}")
    int cancellationDeadlineMinute;

    @Value("${STORE.PICKUP_DEADLINE_HOUR:20}")
    int pickupDeadlineHour;

    @Value("${STORE.PICKUP_DEADLINE_MINUTE:0}")
    int pickupDeadlineMinute;

    @PostConstruct
    void validateTimeRange() {
        validateHour(reservationDeadlineHour, "RESERVATION_DEADLINE_HOUR");
        validateHour(cancellationDeadlineHour, "CANCELLATION_DEADLINE_HOUR");
        validateHour(pickupDeadlineHour, "PICKUP_DEADLINE_HOUR");
        validateMinute(reservationDeadlineMinute, "RESERVATION_DEADLINE_MINUTE");
        validateMinute(cancellationDeadlineMinute, "CANCELLATION_DEADLINE_MINUTE");
        validateMinute(pickupDeadlineMinute, "PICKUP_DEADLINE_MINUTE");

        int cancellationTotal = cancellationDeadlineHour * 60 + cancellationDeadlineMinute;
        int reservationTotal = reservationDeadlineHour * 60 + reservationDeadlineMinute;
        int pickupTotal = pickupDeadlineHour * 60 + pickupDeadlineMinute;

        if (cancellationTotal > reservationTotal || reservationTotal > pickupTotal) {
            throw new IllegalStateException(
                "STORE time order must be: CANCELLATION_DEADLINE <= RESERVATION_DEADLINE <= PICKUP_DEADLINE.");
        }
    }

    private void validateHour(int hour, String key) {
        if (hour < 0 || hour > 27) {
            throw new IllegalStateException("STORE " + key + " must be between 0 and 27.");
        }
    }

    private void validateMinute(int minute, String key) {
        if (minute < 0 || minute > 59) {
            throw new IllegalStateException("STORE " + key + " must be between 0 and 59.");
        }
    }
}
