package store.onuljang.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import store.onuljang.appservice.DeliveryAppService;
import store.onuljang.controller.request.DeliveryReadyRequest;
import store.onuljang.exception.UserValidateException;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;
import store.onuljang.support.IntegrationTestBase;
import store.onuljang.util.TimeUtil;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@TestPropertySource(properties = {
    "KAKAOPAY.ENABLED=false",
    "KAKAOPAY.ADMIN_KEY=test-admin-key",
    "KAKAOPAY.CID=test-cid",
    "KAKAOPAY.APPROVAL_URL=http://localhost/approve",
    "KAKAOPAY.CANCEL_URL=http://localhost/cancel",
    "KAKAOPAY.FAIL_URL=http://localhost/fail",
    "KAKAOPAY.HOST=https://kapi.kakao.com"
})
class DeliveryAppServiceTest extends IntegrationTestBase {

    @Autowired
    DeliveryAppService deliveryAppService;

    @Autowired
    DeliveryOrderService deliveryOrderService;

    @MockBean
    KakaoLocalService kakaoLocalService;

    private static final String ADDRESS = "서울 강서구 과일맛집";

    @BeforeEach
    void setUpClock() {
        ZonedDateTime fixed = ZonedDateTime.of(LocalDate.of(2026, 1, 21), LocalTime.of(10, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(fixed.toInstant(), TimeUtil.KST));
    }

    @AfterEach
    void resetClock() {
        TimeUtil.resetClock();
    }

    @Test
    void ready_appliesNearFee_withinOneKm() {
        Users user = testFixture.createUser("배달고객");
        Product product = testFixture.createTodayProduct("딸기", 5, new BigDecimal("15000"),
            testFixture.createDefaultAdmin());
        Reservation reservation = testFixture.createReservation(user, product, 1);

        given(kakaoLocalService.geocodeAddress(ADDRESS))
            .willReturn(Optional.of(new KakaoLocalService.Coordinate(37.556904, 126.8372613)));

        DeliveryReadyRequest request = new DeliveryReadyRequest(
            java.util.List.of(reservation.getId()),
            12,
            "01012345678",
            "12345",
            ADDRESS,
            "101호"
        );

        long orderId = deliveryAppService.ready(user.getUid(), request).orderId();
        DeliveryOrder order = deliveryOrderService.findByIdWithLock(orderId);

        assertThat(order.getDeliveryFee()).isEqualByComparingTo(new BigDecimal("2900"));
        assertThat(order.getDistanceKm()).isNotNull();
        assertThat(order.getDistanceKm()).isLessThanOrEqualTo(new BigDecimal("1.000"));
    }

    @Test
    void ready_appliesFarFee_withinTwoKm() {
        Users user = testFixture.createUser("배달고객");
        Product product = testFixture.createTodayProduct("포도", 5, new BigDecimal("15000"),
            testFixture.createDefaultAdmin());
        Reservation reservation = testFixture.createReservation(user, product, 1);

        given(kakaoLocalService.geocodeAddress(ADDRESS))
            .willReturn(Optional.of(new KakaoLocalService.Coordinate(37.568504, 126.8372613)));

        DeliveryReadyRequest request = new DeliveryReadyRequest(
            java.util.List.of(reservation.getId()),
            13,
            "01012345678",
            "12345",
            ADDRESS,
            "101호"
        );

        long orderId = deliveryAppService.ready(user.getUid(), request).orderId();
        DeliveryOrder order = deliveryOrderService.findByIdWithLock(orderId);

        assertThat(order.getDeliveryFee()).isEqualByComparingTo(new BigDecimal("3900"));
        assertThat(order.getDistanceKm()).isGreaterThan(new BigDecimal("1.000"));
        assertThat(order.getDistanceKm()).isLessThanOrEqualTo(new BigDecimal("2.000"));
    }

    @Test
    void ready_rejects_whenDistanceOverTwoKm() {
        Users user = testFixture.createUser("배달고객");
        Product product = testFixture.createTodayProduct("사과", 5, new BigDecimal("15000"),
            testFixture.createDefaultAdmin());
        Reservation reservation = testFixture.createReservation(user, product, 1);

        given(kakaoLocalService.geocodeAddress(ADDRESS))
            .willReturn(Optional.of(new KakaoLocalService.Coordinate(37.586504, 126.8372613)));

        DeliveryReadyRequest request = new DeliveryReadyRequest(
            java.util.List.of(reservation.getId()),
            14,
            "01012345678",
            "12345",
            ADDRESS,
            "101호"
        );

        assertThatThrownBy(() -> deliveryAppService.ready(user.getUid(), request))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("배달 가능 거리(2km)를 초과했습니다.");
    }
}
