package store.onuljang.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store.onuljang.appservice.DeliveryAppService;
import store.onuljang.controller.request.DeliveryReadyRequest;
import store.onuljang.exception.UserValidateException;
import store.onuljang.repository.DeliveryOrderRepository;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.DeliveryStatus;
import store.onuljang.support.IntegrationTestBase;
import store.onuljang.util.TimeUtil;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@TestPropertySource(properties = {
    "KAKAOPAY.ENABLED=false",
    "KAKAOPAY.SECRET_KEY=test-secret-key",
    "KAKAOPAY.CID=test-cid",
    "KAKAOPAY.APPROVAL_URL=http://localhost/approve",
    "KAKAOPAY.CANCEL_URL=http://localhost/cancel",
    "KAKAOPAY.FAIL_URL=http://localhost/fail",
    "KAKAOPAY.HOST=https://open-api.kakaopay.com"
})
class DeliveryAppServiceTest extends IntegrationTestBase {

    @Autowired
    DeliveryAppService deliveryAppService;

    @Autowired
    DeliveryOrderService deliveryOrderService;

    @Autowired
    DeliveryOrderRepository deliveryOrderRepository;

    @MockitoBean
    KakaoLocalService kakaoLocalService;

    private static final String ADDRESS = "서울 강서구 과일맛집";

    private static final double storeLat = 37.556504;
    private static final double storeLng = 126.8372613;

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
            .willReturn(new KakaoLocalService.Coordinate(37.556904, 126.8372613));

        DeliveryReadyRequest request = new DeliveryReadyRequest(
            List.of(reservation.getId()),
            12,
            0,
            "01012345678",
            "12345",
            ADDRESS,
            "101호",
            storeLat,
            storeLng,
            "test-key-1"
        );

        long orderId = deliveryAppService.ready(user.getUid(), request).orderId();
        DeliveryOrder order = deliveryOrderService.findById(orderId);

        BigDecimal expectedFee = calculateFee(storeLat, storeLng, 37.556904, 126.8372613);
        assertThat(order.getDeliveryFee()).isEqualByComparingTo(expectedFee);
        assertThat(order.getDistanceKm()).isNotNull();
        assertThat(order.getDistanceKm()).isLessThanOrEqualTo(new BigDecimal("1.500"));
    }

    @Test
    void ready_appliesFarFee_withinTwoKm() {
        double otherLat = 37.5319560847746;
        double otherLng = 126.846611592059;

        Users user = testFixture.createUser("배달고객");
        Product product = testFixture.createTodayProduct("포도", 5, new BigDecimal("15000"),
            testFixture.createDefaultAdmin());
        Reservation reservation = testFixture.createReservation(user, product, 1);

        given(kakaoLocalService.geocodeAddress(ADDRESS))
            .willReturn(new KakaoLocalService.Coordinate(otherLat, otherLat));

        DeliveryReadyRequest request = new DeliveryReadyRequest(
            List.of(reservation.getId()),
            13,
            0,
            "01012345678",
            "12345",
            ADDRESS,
            "101호",
            otherLat,
            otherLng,
            "test-key-2"
        );

        long orderId = deliveryAppService.ready(user.getUid(), request).orderId();
        DeliveryOrder order = deliveryOrderService.findById(orderId);

        BigDecimal expectedFee = calculateFee(storeLat, storeLng, otherLat, otherLng);
        assertThat(order.getDeliveryFee()).isEqualByComparingTo(expectedFee);
        assertThat(order.getDistanceKm()).isGreaterThan(new BigDecimal("1.500"));
        assertThat(order.getDistanceKm()).isLessThanOrEqualTo(new BigDecimal("3.000"));
    }

    @Test
    void ready_rejects_whenDistanceOverThreeKm() {
        Users user = testFixture.createUser("배달고객");
        Product product = testFixture.createTodayProduct("사과", 5, new BigDecimal("15000"),
            testFixture.createDefaultAdmin());
        Reservation reservation = testFixture.createReservation(user, product, 1);

        given(kakaoLocalService.geocodeAddress(ADDRESS))
            .willReturn(new KakaoLocalService.Coordinate(37.586504, 126.8372613));

        DeliveryReadyRequest request = new DeliveryReadyRequest(
            List.of(reservation.getId()),
            14,
            0,
            "01012345678",
            "12345",
            ADDRESS,
            "101호",
            storeLat + 10.0,
            storeLng + 10.0,
            "test-key-3"
        );

        assertThatThrownBy(() -> deliveryAppService.ready(user.getUid(), request))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("배달 가능 거리(3km)를 초과했습니다.");
    }

    private BigDecimal calculateFee(double storeLat, double storeLng, double targetLat, double targetLng) {
        double distance = getDistanceKm(storeLat, storeLng, targetLat, targetLng);
        if (distance <= 1.5d) {
            return new BigDecimal("2900");
        }
        double extraKm = Math.max(0, distance - 1.5d);
        long extraUnits = (long) Math.ceil(extraKm / 0.1d);
        return new BigDecimal("2900").add(new BigDecimal("50").multiply(BigDecimal.valueOf(extraUnits)));
    }

    private double getDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371 * c;
    }

    @Test
    void ready_cancelsExistingPendingPayment_whenNewOrderCreated() {
        // Arrange
        Users user = testFixture.createUser("배달고객2");
        Product product = testFixture.createTodayProduct("수박", 10, new BigDecimal("15000"),
            testFixture.createDefaultAdmin());
        Reservation oldReservation = testFixture.createReservation(user, product, 1);
        DeliveryOrder oldOrder = testFixture.createDeliveryOrder(user, oldReservation, DeliveryStatus.PENDING_PAYMENT);
        long oldOrderId = oldOrder.getId();

        Product newProduct = testFixture.createTodayProduct("참외", 10, new BigDecimal("15000"),
            testFixture.createDefaultAdmin());
        Reservation newReservation = testFixture.createReservation(user, newProduct, 1);

        DeliveryReadyRequest request = new DeliveryReadyRequest(
            List.of(newReservation.getId()),
            12, 0,
            "01012345678", "12345", ADDRESS, "101호",
            storeLat, storeLng,
            "test-key-cancel-1"
        );

        // Act
        deliveryAppService.ready(user.getUid(), request);

        // Assert
        DeliveryOrder updatedOld = deliveryOrderService.findById(oldOrderId);
        assertThat(updatedOld.getStatus()).isEqualTo(DeliveryStatus.CANCELED);
    }

    @Test
    void ready_doesNotAffectPaidOrder_whenNewOrderCreated() {
        // Arrange
        Users user = testFixture.createUser("배달고객3");
        Product product = testFixture.createTodayProduct("포도2", 10, new BigDecimal("15000"),
            testFixture.createDefaultAdmin());
        Reservation paidReservation = testFixture.createReservation(user, product, 1);
        DeliveryOrder paidOrder = testFixture.createDeliveryOrder(user, paidReservation, DeliveryStatus.PAID);
        long paidOrderId = paidOrder.getId();

        Product newProduct = testFixture.createTodayProduct("사과2", 10, new BigDecimal("15000"),
            testFixture.createDefaultAdmin());
        Reservation newReservation = testFixture.createReservation(user, newProduct, 1);

        DeliveryReadyRequest request = new DeliveryReadyRequest(
            List.of(newReservation.getId()),
            12, 0,
            "01012345678", "12345", ADDRESS, "101호",
            storeLat, storeLng,
            "test-key-cancel-2"
        );

        // Act
        deliveryAppService.ready(user.getUid(), request);

        // Assert
        DeliveryOrder updatedPaid = deliveryOrderService.findById(paidOrderId);
        assertThat(updatedPaid.getStatus()).isEqualTo(DeliveryStatus.PAID);
    }

    @Test
    void cancel_pendingPaymentOrder_succeeds() {
        // Arrange
        Users user = testFixture.createUser("취소고객");
        Product product = testFixture.createTodayProduct("딸기", 5, new BigDecimal("15000"),
            testFixture.createDefaultAdmin());
        Reservation reservation = testFixture.createReservation(user, product, 1);
        DeliveryOrder order = testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PENDING_PAYMENT);

        // Act
        deliveryAppService.cancel(user.getUid(), order.getId());

        // Assert
        DeliveryOrder updated = deliveryOrderService.findById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.CANCELED);
    }

    @Test
    void cancel_paidOrder_throwsValidateException() {
        // Arrange
        Users user = testFixture.createUser("취소고객2");
        Product product = testFixture.createTodayProduct("사과", 5, new BigDecimal("15000"),
            testFixture.createDefaultAdmin());
        Reservation reservation = testFixture.createReservation(user, product, 1);
        DeliveryOrder order = testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PAID);

        // Act & Assert
        assertThatThrownBy(() -> deliveryAppService.cancel(user.getUid(), order.getId()))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("이미 결제 완료된 주문입니다.");
    }

    @Test
    void fail_pendingPaymentOrder_succeeds() {
        // Arrange
        Users user = testFixture.createUser("실패고객");
        Product product = testFixture.createTodayProduct("포도", 5, new BigDecimal("15000"),
            testFixture.createDefaultAdmin());
        Reservation reservation = testFixture.createReservation(user, product, 1);
        DeliveryOrder order = testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PENDING_PAYMENT);

        // Act
        deliveryAppService.fail(user.getUid(), order.getId());

        // Assert
        DeliveryOrder updated = deliveryOrderService.findById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.FAILED);
    }

    @Test
    void fail_paidOrder_throwsValidateException() {
        // Arrange
        Users user = testFixture.createUser("실패고객2");
        Product product = testFixture.createTodayProduct("수박", 5, new BigDecimal("15000"),
            testFixture.createDefaultAdmin());
        Reservation reservation = testFixture.createReservation(user, product, 1);
        DeliveryOrder order = testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PAID);

        // Act & Assert
        assertThatThrownBy(() -> deliveryAppService.fail(user.getUid(), order.getId()))
            .isInstanceOf(UserValidateException.class)
            .hasMessageContaining("이미 결제 완료된 주문입니다.");
    }
}
