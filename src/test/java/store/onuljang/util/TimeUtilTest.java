package store.onuljang.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import store.onuljang.shared.util.TimeUtil;
import static store.onuljang.shared.util.TimeUtil.*;

/**
 * TimeUtil 단위 테스트
 */
class TimeUtilTest {

    @Test
    @DisplayName("nowDate - KST 기준 오늘 날짜 반환")
    void nowDate_ReturnsKSTToday() {
        // when
        LocalDate result = nowDate();

        // then
        assertThat(result).isNotNull();
        assertThat(result).isBeforeOrEqualTo(LocalDate.now(KST).plusDays(1));
    }

    @Test
    @DisplayName("yesterdayDate - KST 기준 어제 날짜 반환")
    void yesterdayDate_ReturnsKSTYesterday() {
        // when
        LocalDate result = yesterdayDate();

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(nowDate().minusDays(1));
    }

    @Test
    @DisplayName("tomorrowDate - KST 기준 내일 날짜 반환")
    void tomorrowDate_ReturnsKSTTomorrow() {
        // when
        LocalDate result = tomorrowDate();

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(nowDate().plusDays(1));
    }

    @Test
    @DisplayName("nowDateTime - KST 기준 현재 날짜시간 반환")
    void nowDateTime_ReturnsKSTNow() {
        // when
        var result = nowDateTime();

        // then
        assertThat(result).isNotNull();
        assertThat(result.toLocalDate()).isEqualTo(nowDate());
    }

    @Test
    @DisplayName("isPastDate - 과거 날짜는 true 반환")
    void isPastDate_WithPastDate_ReturnsTrue() {
        // given
        LocalDate pastDate = nowDate().minusDays(1);

        // when
        boolean result = TimeUtil.isPastDate(pastDate);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isPastDate - 오늘 날짜는 false 반환")
    void isPastDate_WithToday_ReturnsFalse() {
        // given
        LocalDate today = nowDate();

        // when
        boolean result = TimeUtil.isPastDate(today);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isPastDate - 미래 날짜는 false 반환")
    void isPastDate_WithFutureDate_ReturnsFalse() {
        // given
        LocalDate futureDate = nowDate().plusDays(1);

        // when
        boolean result = TimeUtil.isPastDate(futureDate);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isCancelDeadlineOver - 과거 날짜는 true 반환")
    void isCancelDeadlineOver_WithPastDate_ReturnsTrue() {
        // given
        LocalDate pastDate = nowDate().minusDays(1);

        // when
        boolean result = TimeUtil.isCancelDeadlineOver(pastDate);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isCancelDeadlineOver - 미래 날짜는 false 반환")
    void isCancelDeadlineOver_WithFutureDate_ReturnsFalse() {
        // given
        LocalDate futureDate = nowDate().plusDays(2);

        // when
        boolean result = TimeUtil.isCancelDeadlineOver(futureDate);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isReserveDeadlineOver - 과거 날짜는 true 반환")
    void isReserveDeadlineOver_WithPastDate_ReturnsTrue() {
        // given
        LocalDate pastDate = nowDate().minusDays(1);

        // when
        boolean result = TimeUtil.isReserveDeadlineOver(pastDate);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isReserveDeadlineOver - 미래 날짜는 false 반환")
    void isReserveDeadlineOver_WithFutureDate_ReturnsFalse() {
        // given
        LocalDate futureDate = nowDate().plusDays(2);

        // when
        boolean result = TimeUtil.isReserveDeadlineOver(futureDate);

        // then
        assertThat(result).isFalse();
    }

    // === resolveDeadline ===

    @Test
    @DisplayName("resolveDeadline - 일반 시간(19시)은 같은 날 반환")
    void resolveDeadline_NormalHour_ReturnsSameDay() {
        // given
        LocalDate date = LocalDate.of(2025, 6, 15);

        // when
        java.time.ZonedDateTime result = TimeUtil.resolveDeadline(date, 19, 30);

        // then
        assertThat(result.toLocalDate()).isEqualTo(date);
        assertThat(result.getHour()).isEqualTo(19);
        assertThat(result.getMinute()).isEqualTo(30);
    }

    @Test
    @DisplayName("resolveDeadline - 24시는 익일 0시 반환")
    void resolveDeadline_Hour24_ReturnsNextDay0() {
        // given
        LocalDate date = LocalDate.of(2025, 6, 15);

        // when
        java.time.ZonedDateTime result = TimeUtil.resolveDeadline(date, 24, 0);

        // then
        assertThat(result.toLocalDate()).isEqualTo(date.plusDays(1));
        assertThat(result.getHour()).isEqualTo(0);
        assertThat(result.getMinute()).isEqualTo(0);
    }

    @Test
    @DisplayName("resolveDeadline - 25시는 익일 1시 반환")
    void resolveDeadline_Hour25_ReturnsNextDay1() {
        // given
        LocalDate date = LocalDate.of(2025, 6, 15);

        // when
        java.time.ZonedDateTime result = TimeUtil.resolveDeadline(date, 25, 0);

        // then
        assertThat(result.toLocalDate()).isEqualTo(date.plusDays(1));
        assertThat(result.getHour()).isEqualTo(1);
        assertThat(result.getMinute()).isEqualTo(0);
    }

    @Test
    @DisplayName("resolveDeadline - 27시는 익일 3시 반환")
    void resolveDeadline_Hour27_ReturnsNextDay3() {
        // given
        LocalDate date = LocalDate.of(2025, 6, 15);

        // when
        java.time.ZonedDateTime result = TimeUtil.resolveDeadline(date, 27, 0);

        // then
        assertThat(result.toLocalDate()).isEqualTo(date.plusDays(1));
        assertThat(result.getHour()).isEqualTo(3);
        assertThat(result.getMinute()).isEqualTo(0);
    }

    // === isDeadlineOver ===

    @Test
    @DisplayName("isDeadlineOver - 마감 전이면 false")
    void isDeadlineOver_BeforeDeadline_ReturnsFalse() {
        // given
        LocalDate futureDate = nowDate().plusDays(2);

        // when
        boolean result = TimeUtil.isDeadlineOver(futureDate, 19, 0);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isDeadlineOver - 마감 후이면 true")
    void isDeadlineOver_AfterDeadline_ReturnsTrue() {
        // given
        LocalDate pastDate = nowDate().minusDays(1);

        // when
        boolean result = TimeUtil.isDeadlineOver(pastDate, 19, 0);

        // then
        assertThat(result).isTrue();
    }

    // === isBusinessDayPast ===

    @Test
    @DisplayName("isBusinessDayPast - 과거 영업일이면 true")
    void isBusinessDayPast_PastBusinessDay_ReturnsTrue() {
        // given
        LocalDate pastDate = nowDate().minusDays(2);

        // when
        boolean result = TimeUtil.isBusinessDayPast(pastDate, 20, 0);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isBusinessDayPast - 미래 영업일이면 false")
    void isBusinessDayPast_FutureBusinessDay_ReturnsFalse() {
        // given
        LocalDate futureDate = nowDate().plusDays(2);

        // when
        boolean result = TimeUtil.isBusinessDayPast(futureDate, 20, 0);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isBusinessDayPast - cross-midnight 마감(25시) 아직 안 지남")
    void isBusinessDayPast_CrossMidnight_BeforeDeadline_ReturnsFalse() {
        // given - 미래 날짜의 25시 마감은 분명히 아직 안 지남
        LocalDate futureDate = nowDate().plusDays(1);

        // when
        boolean result = TimeUtil.isBusinessDayPast(futureDate, 25, 0);

        // then
        assertThat(result).isFalse();
    }
}
