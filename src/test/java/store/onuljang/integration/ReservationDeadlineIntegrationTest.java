package store.onuljang.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store.onuljang.controller.request.ReservationRequest;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.Users;
import store.onuljang.support.IntegrationTestBase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

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
    }

    @Test
    @DisplayName("서버 기준 내일 상품 예약은 성공해야 함 (데드라인 전)")
    void reserve_FutureProduct_Success() throws Exception {
        // given
        Product product = testFixture.createTomorrowProduct("내일상품", 10, new BigDecimal("10000"), admin);
        ReservationRequest request = new ReservationRequest(product.getId(), 1, new BigDecimal("10000"));

        // when
        var response = postAction("/api/auth/reservations/", request, accessToken, Long.class);

        // then
        assertThat(response.isOk()).isTrue();
    }

    @Test
    @DisplayName("서버 기준 어제 상품 예약은 실패해야 함 (19:30 데드라인 경과)")
    void reserve_PastProduct_Fail() throws Exception {
        // given
        Product product = testFixture.createPastProduct("어제상품", 10, new BigDecimal("10000"), 1, admin);
        ReservationRequest request = new ReservationRequest(product.getId(), 1, new BigDecimal("10000"));

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
        LocalDateTime now = LocalDateTime.now();

        // 1. 게시 시간 1분 전 -> 실패
        Product before = testFixture.createProductAtDateTime("게시전", 10, new BigDecimal("1000"), now.plusMinutes(1),
                admin);
        var resBefore = postAction("/api/auth/reservations/",
                new ReservationRequest(before.getId(), 1, new BigDecimal("1000")), accessToken, ErrorResponse.class);
        assertThat(resBefore.isBadRequest()).isTrue();
        assertThat(resBefore.body().message()).isEqualTo("상품 게시 시간 전입니다.");

        // 2. 게시 시간 1분 후 -> 성공
        Product after = testFixture.createProductAtDateTime("게시후", 10, new BigDecimal("1000"), now.minusMinutes(1),
                admin);
        var resAfter = postAction("/api/auth/reservations/",
                new ReservationRequest(after.getId(), 1, new BigDecimal("1000")), accessToken, Long.class);
        assertThat(resAfter.isOk()).isTrue();
    }

    @Test
    @DisplayName("마감 시간(19:30) 경계성 테스트 - 오늘 상품 기준")
    void reserve_ReserveDeadline_Today_Boundaries() throws Exception {
        LocalTime nowTime = LocalTime.now();
        LocalTime deadline = LocalTime.of(19, 30);

        if (nowTime.isBefore(deadline)) {
            // 현재가 19:30 전이면 오늘 상품 예약 가능해야 함
            Product today = testFixture.createTodayProduct("오늘상품", 10, new BigDecimal("1000"), admin);
            var res = postAction("/api/auth/reservations/",
                    new ReservationRequest(today.getId(), 1, new BigDecimal("1000")), accessToken, Long.class);
            assertThat(res.isOk()).isTrue();
        } else {
            // 현재가 19:30 후면 오늘 상품 예약 실패해야 함
            Product today = testFixture.createTodayProduct("오늘상품", 10, new BigDecimal("1000"), admin);
            var res = postAction("/api/auth/reservations/",
                    new ReservationRequest(today.getId(), 1, new BigDecimal("1000")), accessToken, ErrorResponse.class);
            assertThat(res.isBadRequest()).isTrue();
            assertThat(res.body().message()).isEqualTo("예약 가능한 시간이 지났습니다.");
        }
    }
}
