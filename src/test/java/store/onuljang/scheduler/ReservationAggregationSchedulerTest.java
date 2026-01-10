package store.onuljang.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.support.TestFixture;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * ReservationAggregationScheduler 테스트
 *
 * 매일 00:10에 예약 데이터 집계
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationAggregationSchedulerTest {

    @Autowired
    private ReservationAggregationScheduler reservationAggregationScheduler;

    @Autowired
    private TestFixture testFixture;

    private Admin admin;
    private Users user;

    @BeforeEach
    void setUp() {
        admin = testFixture.createDefaultAdmin();
        user = testFixture.createUser("테스트유저");
    }

    @Test
    @DisplayName("예약 집계 실행 - 정상 동작")
    void aggregate_Success() {
        // given - PICKED 상태의 예약 데이터 생성
        Product product = testFixture.createTodayProduct("테스트상품", 10, new BigDecimal("10000"), admin);
        testFixture.createReservationWithStatus(user, product, 2, ReservationStatus.PICKED);
        testFixture.createReservationWithStatus(user, product, 3, ReservationStatus.PICKED);

        // when & then - 예외 없이 정상 동작
        assertThatCode(() -> reservationAggregationScheduler.aggregate()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("데이터가 없는 경우에도 정상 동작")
    void aggregate_NoData() {
        // when & then - 데이터 없어도 예외 발생하지 않음
        assertThatCode(() -> reservationAggregationScheduler.aggregate()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("다양한 상태의 예약이 있는 경우 정상 동작")
    void aggregate_WithVariousStatuses() {
        // given
        Product product = testFixture.createTodayProduct("테스트상품", 20, new BigDecimal("15000"), admin);
        testFixture.createReservationWithStatus(user, product, 2, ReservationStatus.PENDING);
        testFixture.createReservationWithStatus(user, product, 3, ReservationStatus.PICKED);
        testFixture.createReservationWithStatus(user, product, 1, ReservationStatus.CANCELED);
        testFixture.createReservationWithStatus(user, product, 1, ReservationStatus.NO_SHOW);

        // when & then
        assertThatCode(() -> reservationAggregationScheduler.aggregate()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("집계 이후 취소가 발생한 경우 정상 동작")
    void aggregate_WithCancellationAfterAggregation() {
        // given - PICKED 상태의 예약 생성 및 첫 번째 집계 실행
        Product product = testFixture.createTodayProduct("테스트상품", 20, new BigDecimal("10000"), admin);
        testFixture.createReservationWithStatus(user, product, 5, ReservationStatus.PICKED);
        testFixture.createReservationWithStatus(user, product, 3, ReservationStatus.PICKED);

        // 첫 번째 집계 실행
        assertThatCode(() -> reservationAggregationScheduler.aggregate()).doesNotThrowAnyException();

        // 집계 후 새로운 예약 생성 및 취소 발생
        testFixture.createReservationWithStatus(user, product, 2, ReservationStatus.PICKED);
        testFixture.createReservationWithStatus(user, product, 1, ReservationStatus.CANCELED);

        // when & then - 두 번째 집계 실행 시에도 정상 동작
        assertThatCode(() -> reservationAggregationScheduler.aggregate()).doesNotThrowAnyException();
    }
}
