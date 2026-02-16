package store.onuljang.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store.onuljang.controller.request.ReservationRequest;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.Users;
import store.onuljang.support.IntegrationTestBase;
import store.onuljang.util.TimeUtil;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static store.onuljang.util.TimeUtil.KST;
import static store.onuljang.util.TimeUtil.nowDateTime;

class ReservationDeadlineIntegrationTest extends IntegrationTestBase {

    private record ErrorResponse(String state, String message) {
    }

    private Users user;
    private String accessToken;
    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = testFixture.createDefaultAdmin();
        user = testFixture.createUser("데드라인테스터");
        accessToken = testFixture.createAccessToken(user);

        // 모든 테스트 시간을 오늘 12:00으로 고정 (19:30 데드라인 전)
        fixedTodayTime(12, 0);
    }

    private void fixedTodayTime(int hour, int minute) {
        LocalDateTime todayNoon = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute));
        Instant instant = todayNoon.atZone(KST).toInstant();
        TimeUtil.setClock(Clock.fixed(instant, KST));
    }

    @AfterEach
    void tearDown() {
        store.onuljang.util.TimeUtil.resetClock();
    }

    @Test
    @DisplayName("서버 기준 내일 상품 예약은 성공해야 함 (데드라인 전)")
    void reserve_FutureProduct_Success() throws Exception {
        // given
        Product product = testFixture.createTomorrowProduct("내일상품", 10, new BigDecimal("10000"), admin);
        ReservationRequest request = new ReservationRequest(product.getId(), 1);

        // when
        var response = postAction("/api/auth/reservations/", request, accessToken, Void.class);

        // then
        assertThat(response.isOk()).isTrue();
    }

    @Test
    @DisplayName("서버 기준 어제 상품 예약은 실패해야 함 (19:30 데드라인 경과)")
    void reserve_PastProduct_Fail() throws Exception {
        // given
        Product product = testFixture.createPastProduct("어제상품", 10, new BigDecimal("10000"), 1, admin);
        ReservationRequest request = new ReservationRequest(product.getId(), 1);

        // when
        var response = postAction("/api/auth/reservations/", request, accessToken, Void.class);

        // then
        assertThat(response.isBadRequest()).isTrue();
        var error = postAction("/api/auth/reservations/", request, accessToken, ErrorResponse.class);
        assertThat(error.body().message()).isEqualTo("예약 가능한 시간이 지났습니다.");
    }

    @Test
    @DisplayName("게시 시간(sellTime) 경계값 테스트")
    void reserve_SellTime_Boundaries() throws Exception {
        LocalDateTime now = nowDateTime();

        // 1. 게시 시간 1분 전 -> 실패
        Product before = testFixture.createProductAtDateTime("게시전", 10, new BigDecimal("1000"), now.plusMinutes(1),
                admin);
        var resBefore = postAction("/api/auth/reservations/",
                new ReservationRequest(before.getId(), 1), accessToken, Object.class);

        assertThat(resBefore.status()).isEqualTo(400);
        ErrorResponse error = objectMapper.convertValue(resBefore.body(), ErrorResponse.class);
        assertThat(error.message()).isEqualTo("상품 게시 시간 전입니다.");

        // 2. 게시 시간 1분 후 -> 성공
        Product after = testFixture.createProductAtDateTime("게시후", 10, new BigDecimal("1000"), now.minusMinutes(1),
                admin);
        var resAfter = postAction("/api/auth/reservations/",
                new ReservationRequest(after.getId(), 1), accessToken, Void.class);
        assertThat(resAfter.isOk()).isTrue();
    }

    @Test
    @DisplayName("마감 시간(19:30) 경계성 테스트 - 오늘 상품 기준")
    void reserve_ReserveDeadline_Today_Boundaries() throws Exception {
        // 1. 19:29분 (마감 전) -> 성공
        fixedTodayTime(19, 29);
        Product todayBefore = testFixture.createTodayProduct("오늘마감전", 10, new BigDecimal("1000"), admin);
        var resBefore = postAction("/api/auth/reservations/",
                new ReservationRequest(todayBefore.getId(), 1), accessToken, Void.class);
        assertThat(resBefore.isOk()).isTrue();

        // 2. 19:31분 (마감 후) -> 실패
        fixedTodayTime(19, 31);
        Product todayAfter = testFixture.createTodayProduct("오늘마감후", 10, new BigDecimal("1000"), admin);
        var resAfter = postAction("/api/auth/reservations/",
                new ReservationRequest(todayAfter.getId(), 1), accessToken, Object.class);

        assertThat(resAfter.status()).isEqualTo(400);
        ErrorResponse error = objectMapper.convertValue(resAfter.body(), ErrorResponse.class);
        assertThat(error.message()).isEqualTo("예약 가능한 시간이 지났습니다.");
    }
}
