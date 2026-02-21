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
}
