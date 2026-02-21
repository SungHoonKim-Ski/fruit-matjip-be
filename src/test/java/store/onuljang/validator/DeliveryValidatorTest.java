package store.onuljang.validator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.config.TestS3Config;
import store.onuljang.shop.delivery.validator.DeliveryValidator;
import store.onuljang.shared.exception.UserValidateException;
import store.onuljang.shared.user.entity.*;
import store.onuljang.shop.product.entity.*;
import store.onuljang.shop.reservation.entity.*;
import store.onuljang.shop.delivery.entity.*;
import store.onuljang.shop.admin.entity.*;
import store.onuljang.shared.auth.entity.*;
import store.onuljang.shared.repository.entity.*;
import store.onuljang.shared.entity.enums.*;
import store.onuljang.shared.entity.base.*;
import store.onuljang.shared.entity.enums.DeliveryStatus;
import store.onuljang.shared.entity.enums.ReservationStatus;
import store.onuljang.support.TestFixture;
import store.onuljang.shared.util.TimeUtil;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestS3Config.class)
@Transactional
class DeliveryValidatorTest {

    @Autowired
    DeliveryValidator deliveryValidator;

    @Autowired
    TestFixture testFixture;

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = testFixture.createDefaultAdmin();
        ZonedDateTime fixed = ZonedDateTime.of(
            LocalDate.of(2026, 1, 21), LocalTime.of(10, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(fixed.toInstant(), TimeUtil.KST));
    }

    @AfterEach
    void tearDown() {
        TimeUtil.resetClock();
    }

    // --- validateReservations ---

    @Test
    @DisplayName("정상 예약은 검증 통과")
    void validateReservations_validReservation_passes() {
        // given
        Users user = testFixture.createUser("배달테스트");
        Product product = testFixture.createTodayProduct("딸기", 5, new BigDecimal("15000"), admin);
        Reservation reservation = testFixture.createReservation(user, product, 1);

        // when / then
        assertThatCode(() -> deliveryValidator.validateReservations(user, List.of(reservation)))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("오늘이 아닌 예약은 배달 불가")
    void validateReservations_notToday_throwsException() {
        // given
        Users user = testFixture.createUser("배달테스트");
        Product product = testFixture.createTomorrowProduct("사과", 5, new BigDecimal("15000"), admin);
        Reservation reservation = testFixture.createReservation(user, product, 1);

        // when / then
        assertThatThrownBy(() -> deliveryValidator.validateReservations(user, List.of(reservation)))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("오늘 수령 예약만 가능");
    }

    @Test
    @DisplayName("다른 유저의 예약은 배달 불가")
    void validateReservations_otherUser_throwsException() {
        // given
        Users owner = testFixture.createUser("원래유저");
        Users other = testFixture.createUser("다른유저");
        Product product = testFixture.createTodayProduct("포도", 5, new BigDecimal("15000"), admin);
        Reservation reservation = testFixture.createReservation(owner, product, 1);

        // when / then
        assertThatThrownBy(() -> deliveryValidator.validateReservations(other, List.of(reservation)))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("다른 유저가 예약한 상품");
    }

    @Test
    @DisplayName("PENDING 상태가 아닌 예약은 배달 불가")
    void validateReservations_notPendingStatus_throwsException() {
        // given
        Users user = testFixture.createUser("배달테스트");
        Product product = testFixture.createTodayProduct("수박", 5, new BigDecimal("15000"), admin);
        Reservation reservation = testFixture.createReservationWithStatus(
            user, product, 1, ReservationStatus.PICKED);

        // when / then
        assertThatThrownBy(() -> deliveryValidator.validateReservations(user, List.of(reservation)))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("배달 주문이 불가능한 예약");
    }

    @Test
    @DisplayName("배달 불가 상품이 포함되면 예외")
    void validateReservations_deliveryNotAvailable_throwsException() {
        // given
        Users user = testFixture.createUser("배달테스트");
        Product product = testFixture.createTodayProduct("참외", 5, new BigDecimal("15000"), admin);
        product.toggleDeliveryAvailable();
        Reservation reservation = testFixture.createReservation(user, product, 1);

        // when / then
        assertThatThrownBy(() -> deliveryValidator.validateReservations(user, List.of(reservation)))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("배달 불가 상품");
    }

    @Test
    @DisplayName("배달 마감 시간 이후에는 배달 불가")
    void validateReservations_afterDeadline_throwsException() {
        // given - 20시로 고정 (배달 종료 19:30 이후)
        ZonedDateTime late = ZonedDateTime.of(
            LocalDate.of(2026, 1, 21), LocalTime.of(20, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(late.toInstant(), TimeUtil.KST));

        Users user = testFixture.createUser("배달테스트");
        Product product = testFixture.createTodayProduct("복숭아", 5, new BigDecimal("15000"), admin);
        Reservation reservation = testFixture.createReservation(user, product, 1);

        // when / then
        assertThatThrownBy(() -> deliveryValidator.validateReservations(user, List.of(reservation)))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("배달 주문 가능 시간이 지났습니다");
    }

    @Test
    @DisplayName("이미 진행 중인 배달 주문이 있는 예약은 불가")
    void validateReservations_existingActiveDelivery_throwsException() {
        // given
        Users user = testFixture.createUser("배달테스트");
        Product product = testFixture.createTodayProduct("바나나", 5, new BigDecimal("15000"), admin);
        Reservation reservation = testFixture.createReservation(user, product, 1);
        testFixture.createDeliveryOrderWithLink(user, reservation, DeliveryStatus.PAID);

        // when / then
        assertThatThrownBy(() -> deliveryValidator.validateReservations(user, List.of(reservation)))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("이미 배달 주문이 진행 중");
    }

    @Test
    @DisplayName("취소된 배달 주문의 예약은 재주문 가능")
    void validateReservations_canceledDelivery_passes() {
        // given
        Users user = testFixture.createUser("배달테스트");
        Product product = testFixture.createTodayProduct("키위", 5, new BigDecimal("15000"), admin);
        Reservation reservation = testFixture.createReservation(user, product, 1);
        testFixture.createDeliveryOrderWithLink(user, reservation, DeliveryStatus.CANCELED);

        // when / then
        assertThatCode(() -> deliveryValidator.validateReservations(user, List.of(reservation)))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("실패된 배달 주문의 예약은 재주문 가능")
    void validateReservations_failedDelivery_passes() {
        // given
        Users user = testFixture.createUser("배달테스트");
        Product product = testFixture.createTodayProduct("망고", 5, new BigDecimal("15000"), admin);
        Reservation reservation = testFixture.createReservation(user, product, 1);
        testFixture.createDeliveryOrderWithLink(user, reservation, DeliveryStatus.FAILED);

        // when / then
        assertThatCode(() -> deliveryValidator.validateReservations(user, List.of(reservation)))
            .doesNotThrowAnyException();
    }

    // --- validateDeliveryTime ---

    @Test
    @DisplayName("배달 시간 범위 내 시간은 통과")
    void validateDeliveryTime_withinRange_passes() {
        // given - START_HOUR=12, END_HOUR=19:30
        // when / then
        assertThatCode(() -> deliveryValidator.validateDeliveryTime(15, 0))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("배달 시작 시간과 동일하면 통과")
    void validateDeliveryTime_exactStart_passes() {
        // given - START_HOUR=12, START_MINUTE=0
        // when / then
        assertThatCode(() -> deliveryValidator.validateDeliveryTime(12, 0))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("배달 종료 시간과 동일하면 통과")
    void validateDeliveryTime_exactEnd_passes() {
        // given - END_HOUR=19, END_MINUTE=30
        // when / then
        assertThatCode(() -> deliveryValidator.validateDeliveryTime(19, 30))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("배달 시작 시간 이전은 불가")
    void validateDeliveryTime_beforeStart_throwsException() {
        // when / then
        assertThatThrownBy(() -> deliveryValidator.validateDeliveryTime(11, 0))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("배달 수령 시간은");
    }

    @Test
    @DisplayName("배달 종료 시간 이후는 불가")
    void validateDeliveryTime_afterEnd_throwsException() {
        // when / then
        assertThatThrownBy(() -> deliveryValidator.validateDeliveryTime(20, 0))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("배달 수령 시간은");
    }

    @Test
    @DisplayName("배달 시간이 null이면 예외")
    void validateDeliveryTime_null_throwsException() {
        // when / then
        assertThatThrownBy(() -> deliveryValidator.validateDeliveryTime(null, null))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("배달 수령 시간을 확인");
    }

    // --- validateMinimumAmount ---

    @Test
    @DisplayName("최소 금액 이상이면 통과")
    void validateMinimumAmount_aboveMinimum_passes() {
        // given - MIN_AMOUNT=15000
        // when / then
        assertThatCode(() -> deliveryValidator.validateMinimumAmount(new BigDecimal("15000")))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("최소 금액 초과이면 통과")
    void validateMinimumAmount_overMinimum_passes() {
        // when / then
        assertThatCode(() -> deliveryValidator.validateMinimumAmount(new BigDecimal("20000")))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("최소 금액 미만이면 예외")
    void validateMinimumAmount_belowMinimum_throwsException() {
        // when / then
        assertThatThrownBy(() -> deliveryValidator.validateMinimumAmount(new BigDecimal("14999")))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("원 이상부터 가능");
    }

    // --- validateScheduledDelivery ---

    @Test
    @DisplayName("예약배달 시간이 null이면 일반배달로 검증 통과")
    void validateScheduledDelivery_null_passes() {
        // given - 일반배달 (scheduledHour = null)
        // when / then
        assertThatCode(() -> deliveryValidator.validateScheduledDelivery(null, null))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("유효한 예약배달 시간은 검증 통과")
    void validateScheduledDelivery_validHour_passes() {
        // given - 현재 10:00, 슬롯 15시 (5시간 남음, minSlot=13, maxSlot=19)
        // when / then
        assertThatCode(() -> deliveryValidator.validateScheduledDelivery(15, 0))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("최소 슬롯(startHour+1)과 동일하면 통과")
    void validateScheduledDelivery_minSlot_passes() {
        // given - 현재 10:00, minSlot=13 (3시간 남음)
        // when / then
        assertThatCode(() -> deliveryValidator.validateScheduledDelivery(13, 0))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("최대 슬롯(endHour)과 동일하면 통과")
    void validateScheduledDelivery_maxSlot_passes() {
        // given - 현재 10:00, maxSlot=19 (9시간 남음)
        // when / then
        assertThatCode(() -> deliveryValidator.validateScheduledDelivery(19, 0))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("최소 슬롯 미만이면 예외")
    void validateScheduledDelivery_belowMinSlot_throwsException() {
        // given - minSlot=13, 요청=12
        // when / then
        assertThatThrownBy(() -> deliveryValidator.validateScheduledDelivery(12, 0))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("예약배달 시간은");
    }

    @Test
    @DisplayName("최대 슬롯 초과이면 예외")
    void validateScheduledDelivery_aboveMaxSlot_throwsException() {
        // given - maxSlot=19, 요청=20
        // when / then
        assertThatThrownBy(() -> deliveryValidator.validateScheduledDelivery(20, 0))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("예약배달 시간은");
    }

    @Test
    @DisplayName("접수 마감 시간(endHour-1) 이후에는 예약배달 불가")
    void validateScheduledDelivery_afterCutoff_throwsException() {
        // given - cutoff=18:30 (endTime 19:30 - 1시간), 현재 18:30으로 변경
        ZonedDateTime late = ZonedDateTime.of(
            LocalDate.of(2026, 1, 21), LocalTime.of(18, 30), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(late.toInstant(), TimeUtil.KST));

        // when / then
        assertThatThrownBy(() -> deliveryValidator.validateScheduledDelivery(19, 0))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("까지만 접수 가능");
    }

    @Test
    @DisplayName("접수 마감 직전(cutoff - 1분)에는 예약배달 가능")
    void validateScheduledDelivery_justBeforeCutoff_passes() {
        // given - cutoff=18:30, 현재 17:29로 변경, 슬롯 19:00 (91분 남음)
        ZonedDateTime beforeCutoff = ZonedDateTime.of(
            LocalDate.of(2026, 1, 21), LocalTime.of(17, 29), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(beforeCutoff.toInstant(), TimeUtil.KST));

        // when / then
        assertThatCode(() -> deliveryValidator.validateScheduledDelivery(19, 0))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("현재 시간 기준 1시간 미만 슬롯은 예외")
    void validateScheduledDelivery_lessThanOneHour_throwsException() {
        // given - 현재 13:30, 슬롯 14시 (30분 남음)
        ZonedDateTime now = ZonedDateTime.of(
            LocalDate.of(2026, 1, 21), LocalTime.of(13, 30), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(now.toInstant(), TimeUtil.KST));

        // when / then
        assertThatThrownBy(() -> deliveryValidator.validateScheduledDelivery(14, 0))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("최소 1시간 이후");
    }

    @Test
    @DisplayName("현재 시간 기준 정확히 1시간 슬롯은 통과")
    void validateScheduledDelivery_exactlyOneHour_passes() {
        // given - 현재 14:00, 슬롯 15시 (정확히 60분)
        ZonedDateTime now = ZonedDateTime.of(
            LocalDate.of(2026, 1, 21), LocalTime.of(14, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(now.toInstant(), TimeUtil.KST));

        // when / then
        assertThatCode(() -> deliveryValidator.validateScheduledDelivery(15, 0))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("현재 시간 기준 59분 남은 슬롯은 예외")
    void validateScheduledDelivery_59minutes_throwsException() {
        // given - 현재 14:01, 슬롯 15시 (59분 남음)
        ZonedDateTime now = ZonedDateTime.of(
            LocalDate.of(2026, 1, 21), LocalTime.of(14, 1), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(now.toInstant(), TimeUtil.KST));

        // when / then
        assertThatThrownBy(() -> deliveryValidator.validateScheduledDelivery(15, 0))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("최소 1시간 이후");
    }

    @Test
    @DisplayName("슬롯의 분이 startMinute과 다르면 예외")
    void validateScheduledDelivery_wrongMinute_throwsException() {
        // given - startMinute=0인데 30분으로 요청
        // when / then
        assertThatThrownBy(() -> deliveryValidator.validateScheduledDelivery(15, 30))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("분 단위");
    }
}
