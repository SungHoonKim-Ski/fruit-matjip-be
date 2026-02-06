package store.onuljang.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import store.onuljang.controller.request.DeliveryReadyRequest;
import store.onuljang.repository.entity.*;
import store.onuljang.repository.entity.enums.DeliveryStatus;
import store.onuljang.service.DeliveryOrderService;
import store.onuljang.support.IntegrationTestBase;
import store.onuljang.util.TimeUtil;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import store.onuljang.controller.response.DeliveryReadyResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
    "KAKAOPAY.ENABLED=false",
    "KAKAOPAY.SECRET_KEY=test-secret-key",
    "KAKAOPAY.CID=test-cid",
    "KAKAOPAY.APPROVAL_URL=http://localhost/approve",
    "KAKAOPAY.CANCEL_URL=http://localhost/cancel",
    "KAKAOPAY.FAIL_URL=http://localhost/fail",
    "KAKAOPAY.HOST=https://open-api.kakaopay.com"
})
class DeliveryControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    DeliveryOrderService deliveryOrderService;

    private static final double STORE_LAT = 37.556504;
    private static final double STORE_LNG = 126.8372613;

    @BeforeEach
    void setUpClock() {
        ZonedDateTime fixed = ZonedDateTime.of(
            LocalDate.of(2026, 1, 21), LocalTime.of(10, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(fixed.toInstant(), TimeUtil.KST));
    }

    @AfterEach
    void resetClock() {
        TimeUtil.resetClock();
    }

    @Test
    @DisplayName("GET /config - 배달 설정 조회 성공")
    void getConfig_returnsOk() throws Exception {
        // given
        Users user = testFixture.createUser("배달고객");
        String accessToken = testFixture.createAccessToken(user);

        // when
        ApiResponse<Void> response = getAction(
            "/api/auth/deliveries/config", accessToken, Void.class);

        // then
        assertThat(response.isOk()).isTrue();
    }

    @Test
    @DisplayName("POST /ready - 배달 주문 생성 성공 (KakaoPay 비활성)")
    void ready_withKakaoPayDisabled_returnsOk() throws Exception {
        // given
        Users user = testFixture.createUser("배달고객");
        String accessToken = testFixture.createAccessToken(user);
        Admin admin = testFixture.createDefaultAdmin();
        Product product = testFixture.createTodayProduct("딸기", 5, new BigDecimal("15000"), admin);
        Reservation reservation = testFixture.createReservation(user, product, 1);

        DeliveryReadyRequest request = new DeliveryReadyRequest(
            List.of(reservation.getId()),
            12, 0,
            "01012345678", "12345", "서울 강서구 테스트로", "101호",
            STORE_LAT, STORE_LNG,
            "e2e-test-key-1"
        );

        // when
        ApiResponse<DeliveryReadyResponse> response = postAction(
            "/api/auth/deliveries/ready", request, accessToken, DeliveryReadyResponse.class);

        // then
        assertThat(response.isOk()).isTrue();
        assertThat(response.body()).isNotNull();
        assertThat(response.body().orderId()).isPositive();
    }

    @Test
    @DisplayName("GET /cancel - PENDING_PAYMENT 주문 취소 성공")
    void cancel_pendingOrder_returnsOk() throws Exception {
        // given
        Users user = testFixture.createUser("배달고객");
        String accessToken = testFixture.createAccessToken(user);
        Admin admin = testFixture.createDefaultAdmin();
        Product product = testFixture.createTodayProduct("사과", 5, new BigDecimal("15000"), admin);
        Reservation reservation = testFixture.createReservation(user, product, 1);
        DeliveryOrder order = testFixture.createDeliveryOrder(
            user, reservation, DeliveryStatus.PENDING_PAYMENT);

        // when
        ApiResponse<Void> response = getAction(
            "/api/auth/deliveries/cancel?orderId=" + order.getId(), accessToken, Void.class);

        // then
        assertThat(response.isOk()).isTrue();
        DeliveryOrder updated = deliveryOrderService.findById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.CANCELED);
    }

    @Test
    @DisplayName("GET /cancel - PAID 주문 취소 시 실패")
    void cancel_paidOrder_returnsBadRequest() throws Exception {
        // given
        Users user = testFixture.createUser("배달고객");
        String accessToken = testFixture.createAccessToken(user);
        Admin admin = testFixture.createDefaultAdmin();
        Product product = testFixture.createTodayProduct("포도", 5, new BigDecimal("15000"), admin);
        Reservation reservation = testFixture.createReservation(user, product, 1);
        DeliveryOrder order = testFixture.createDeliveryOrder(
            user, reservation, DeliveryStatus.PAID);

        // when
        ApiResponse<Void> response = getAction(
            "/api/auth/deliveries/cancel?orderId=" + order.getId(), accessToken, Void.class);

        // then
        assertThat(response.isBadRequest()).isTrue();
    }

    @Test
    @DisplayName("GET /fail - PENDING_PAYMENT 주문 실패 처리 성공")
    void fail_pendingOrder_returnsOk() throws Exception {
        // given
        Users user = testFixture.createUser("배달고객");
        String accessToken = testFixture.createAccessToken(user);
        Admin admin = testFixture.createDefaultAdmin();
        Product product = testFixture.createTodayProduct("수박", 5, new BigDecimal("15000"), admin);
        Reservation reservation = testFixture.createReservation(user, product, 1);
        DeliveryOrder order = testFixture.createDeliveryOrder(
            user, reservation, DeliveryStatus.PENDING_PAYMENT);

        // when
        ApiResponse<Void> response = getAction(
            "/api/auth/deliveries/fail?orderId=" + order.getId(), accessToken, Void.class);

        // then
        assertThat(response.isOk()).isTrue();
        DeliveryOrder updated = deliveryOrderService.findById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.FAILED);
    }

    @Test
    @DisplayName("인증 없이 요청 시 403")
    void noAuth_returnsForbidden() throws Exception {
        // when / then
        mockMvc.perform(get("/api/auth/deliveries/config"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /ready - 존재하지 않는 예약 ID로 요청 시 실패")
    void ready_invalidReservationId_returnsBadRequest() throws Exception {
        // given
        Users user = testFixture.createUser("배달고객");
        String accessToken = testFixture.createAccessToken(user);

        DeliveryReadyRequest request = new DeliveryReadyRequest(
            List.of(999999L),
            12, 0,
            "01012345678", "12345", "서울 강서구 테스트로", "101호",
            STORE_LAT, STORE_LNG,
            "e2e-test-key-invalid"
        );

        // when
        ApiResponse<Void> response = postAction(
            "/api/auth/deliveries/ready", request, accessToken, Void.class);

        // then
        assertThat(response.isBadRequest()).isTrue();
    }
}
