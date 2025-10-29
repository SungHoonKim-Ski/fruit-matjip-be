package store.onuljang.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

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

    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now(KST);
    }
}
