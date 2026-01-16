package store.onuljang.util;

import lombok.experimental.UtilityClass;

import java.time.*;

@UtilityClass
public class TimeUtil {
    public static final ZoneId KST = ZoneId.of("Asia/Seoul");
    public final LocalTime SELF_PICK_DEADLINE = LocalTime.of(19, 30);
    public final LocalTime RESERVE_DEADLINE = LocalTime.of(19, 30);
    public final LocalTime CANCEL_DEADLINE = LocalTime.of(19, 30);

    public static LocalDate nowDate() {
        return LocalDate.now(KST);
    }

    public static LocalDate yesterdayDate() {
        return LocalDate.now(KST).minusDays(1);
    }
    public static LocalDate tomorrowDate() {
        return LocalDate.now(KST).plusDays(1);
    }

    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now(KST);
    }

    public boolean isPastDate(LocalDate date) {
        return date.isBefore(nowDate());
    }

    public boolean isCancelDeadlineOver(LocalDate pickupDate) {
        ZonedDateTime deadLine = pickupDate.atTime(CANCEL_DEADLINE).atZone(KST);

        return ZonedDateTime.now(KST).isAfter(deadLine);
    }

    public boolean isReserveDeadlineOver(LocalDate pickupDate) {
        ZonedDateTime deadLine = pickupDate.atTime(RESERVE_DEADLINE).atZone(KST);

        return ZonedDateTime.now(KST).isAfter(deadLine);
    }
}
