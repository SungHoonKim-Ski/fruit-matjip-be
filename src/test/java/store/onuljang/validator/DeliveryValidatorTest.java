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
import store.onuljang.exception.UserValidateException;
import store.onuljang.repository.entity.*;
import store.onuljang.repository.entity.enums.DeliveryStatus;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.support.TestFixture;
import store.onuljang.util.TimeUtil;

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
}
