package store.onuljang.shared.util;

import lombok.experimental.UtilityClass;

import java.time.*;

@UtilityClass
public class TimeUtil {
    public static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static Clock clock = Clock.system(KST);
    @Deprecated
    public final LocalTime RESERVE_DEADLINE = LocalTime.of(19, 30);
    @Deprecated
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

    @Deprecated
    public boolean isCancelDeadlineOver(LocalDate pickupDate) {
        ZonedDateTime deadLine = pickupDate.atTime(CANCEL_DEADLINE).atZone(KST);

        return nowZonedDateTime().isAfter(deadLine);
    }

    @Deprecated
    public boolean isReserveDeadlineOver(LocalDate pickupDate) {
        ZonedDateTime deadLine = pickupDate.atTime(RESERVE_DEADLINE).atZone(KST);

        return nowZonedDateTime().isAfter(deadLine);
    }

    public boolean isAfterDeadline(LocalDate targetDate, LocalTime deadlineTime) {
        ZonedDateTime deadLine = targetDate.atTime(deadlineTime).atZone(KST);
        return nowZonedDateTime().isAfter(deadLine);
    }

    /**
     * 25시 규약 기반 마감 시각 계산.
     * hour >= 24이면 sellDate 익일로 변환 (예: 25:00 → sellDate+1일 01:00)
     */
    public static ZonedDateTime resolveDeadline(LocalDate sellDate, int hour, int minute) {
        if (hour >= 24) {
            return sellDate.plusDays(1).atTime(hour - 24, minute).atZone(KST);
        }
        return sellDate.atTime(hour, minute).atZone(KST);
    }

    /**
     * 현재 시각이 sellDate 기준 마감(hour:minute)을 지났는지 판단.
     */
    public static boolean isDeadlineOver(LocalDate sellDate, int hour, int minute) {
        return nowZonedDateTime().isAfter(resolveDeadline(sellDate, hour, minute));
    }

    /**
     * 영업일 기준 "과거 날짜" 판단 (cross-midnight 대응).
     * pickupDeadline 이후면 해당 영업일은 종료된 것으로 판단.
     */
    public static boolean isBusinessDayPast(LocalDate sellDate, int pickupDeadlineHour, int pickupDeadlineMinute) {
        return nowZonedDateTime().isAfter(resolveDeadline(sellDate, pickupDeadlineHour, pickupDeadlineMinute));
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
