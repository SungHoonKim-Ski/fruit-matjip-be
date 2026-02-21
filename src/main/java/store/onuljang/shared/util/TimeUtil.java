package store.onuljang.shared.util;

import lombok.experimental.UtilityClass;

import java.time.*;

@UtilityClass
public class TimeUtil {
    public static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static Clock clock = Clock.system(KST);
    public final LocalTime RESERVE_DEADLINE = LocalTime.of(19, 30);
    public final LocalTime CANCEL_DEADLINE = LocalTime.of(19, 0);

    public static void setClock(Clock customClock) {
        clock = customClock;
    }

    public static void resetClock() {
        clock = Clock.system(KST);
    }

    public static LocalDate nowDate() {
        return LocalDate.now(clock);
    }

    public static LocalDate yesterdayDate() {
        return nowDate().minusDays(1);
    }

    public static LocalDate tomorrowDate() {
        return nowDate().plusDays(1);
    }

    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now(clock);
    }

    public static ZonedDateTime nowZonedDateTime() {
        return ZonedDateTime.now(clock);
    }

    public static LocalDate dateFromEpochDay(long epochDay) {
        return LocalDate.ofEpochDay(epochDay);
    }

    public boolean isPastDate(LocalDate date) {
        return date.isBefore(nowDate());
    }

    public boolean isCancelDeadlineOver(LocalDate pickupDate) {
        ZonedDateTime deadLine = pickupDate.atTime(CANCEL_DEADLINE).atZone(KST);

        return nowZonedDateTime().isAfter(deadLine);
    }

    public boolean isReserveDeadlineOver(LocalDate pickupDate) {
        ZonedDateTime deadLine = pickupDate.atTime(RESERVE_DEADLINE).atZone(KST);

        return nowZonedDateTime().isAfter(deadLine);
    }

    public boolean isAfterDeadline(LocalDate targetDate, LocalTime deadlineTime) {
        ZonedDateTime deadLine = targetDate.atTime(deadlineTime).atZone(KST);
        return nowZonedDateTime().isAfter(deadLine);
    }

    public String formatTime(int hour, int minute) {
        if (minute <= 0) {
            return hour + "시";
        }
        return hour + "시 " + minute + "분";
    }

    public boolean isBefore(int hour, int minute, int targetHour, int targetMinute) {
        if (hour < targetHour) {
            return true;
        }
        return hour == targetHour && minute < targetMinute;
    }

    public boolean isAfter(int hour, int minute, int targetHour, int targetMinute) {
        if (hour > targetHour) {
            return true;
        }
        return hour == targetHour && minute > targetMinute;
    }
}
