package store.onuljang.scheduler;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shared.config.KakaoPayConfigDto;
import store.onuljang.config.TestS3Config;
import store.onuljang.config.TestSqsConfig;
import store.onuljang.shop.delivery.scheduler.DeliveryPaymentReconciliationScheduler;
import store.onuljang.shared.exception.KakaoPayException;
import store.onuljang.shared.feign.dto.reseponse.KakaoPayOrderResponse;
import store.onuljang.shop.delivery.repository.DeliveryOrderRepository;
import store.onuljang.shop.admin.entity.Admin;
import store.onuljang.shop.delivery.entity.DeliveryOrder;
import store.onuljang.shop.product.entity.Product;
import store.onuljang.shop.reservation.entity.Reservation;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.entity.enums.DeliveryStatus;
import store.onuljang.shared.service.KakaoPayService;
import store.onuljang.support.TestFixture;
import store.onuljang.shared.util.TimeUtil;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestS3Config.class, TestSqsConfig.class})
@Transactional
class DeliveryPaymentReconciliationSchedulerTest {

    @Autowired
    DeliveryPaymentReconciliationScheduler scheduler;

    @Autowired
    DeliveryOrderRepository deliveryOrderRepository;

    @Autowired
    TestFixture testFixture;

    @Autowired
    EntityManager entityManager;

    @MockitoBean
    KakaoPayService kakaoPayService;

    @MockitoBean
    KakaoPayConfigDto kakaoPayConfigDto;

    private Users user;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        ZonedDateTime fixed = ZonedDateTime.of(
                LocalDate.of(2026, 1, 21), LocalTime.of(10, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(fixed.toInstant(), TimeUtil.KST));

        given(kakaoPayConfigDto.isEnabled()).willReturn(true);

        Admin admin = testFixture.createDefaultAdmin();
        user = testFixture.createUser("배달고객");
        Product product = testFixture.createTodayProduct("딸기", 10, new BigDecimal("15000"), admin);
        reservation = testFixture.createReservation(user, product, 1);
    }

    @AfterEach
    void tearDown() {
        TimeUtil.resetClock();
    }

    private DeliveryOrder createPendingOrderWithTid(String tid, int minutesAgo) {
        DeliveryOrder order =
                testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PENDING_PAYMENT);
        order.setKakaoTid(tid);
        order.updateCreatedAt(TimeUtil.nowDateTime().minusMinutes(minutesAgo));
        deliveryOrderRepository.saveAndFlush(order);
        entityManager.clear();
        return order;
    }

    @Test
    @DisplayName("PG 결제 완료 건 → PAID 전환")
    void reconcile_successPayment_marksPaid() {
        // Arrange
        DeliveryOrder order = createPendingOrderWithTid("T_TEST_TID", 3);
        KakaoPayOrderResponse pgResponse = new KakaoPayOrderResponse(
                "T_TEST_TID",
                "test-cid",
                "SUCCESS_PAYMENT",
                List.of(new KakaoPayOrderResponse.PaymentActionDetail("A_APPROVE", "PAYMENT")));
        given(kakaoPayService.order("T_TEST_TID")).willReturn(pgResponse);

        // Act
        scheduler.reconcilePendingPayments();

        // Assert
        DeliveryOrder updated = deliveryOrderRepository.findById(order.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.PAID);
    }

    @Test
    @DisplayName("PG 결제 취소 건 → FAILED 전환")
    void reconcile_cancelPayment_marksFailed() {
        // Arrange
        DeliveryOrder order = createPendingOrderWithTid("T_CANCEL_TID", 3);
        KakaoPayOrderResponse pgResponse =
                new KakaoPayOrderResponse("T_CANCEL_TID", "test-cid", "CANCEL_PAYMENT", null);
        given(kakaoPayService.order("T_CANCEL_TID")).willReturn(pgResponse);

        // Act
        scheduler.reconcilePendingPayments();

        // Assert
        DeliveryOrder updated = deliveryOrderRepository.findById(order.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.FAILED);
    }

    @Test
    @DisplayName("PG 결제 진행 중 → 상태 유지")
    void reconcile_readyStatus_noChange() {
        // Arrange
        DeliveryOrder order = createPendingOrderWithTid("T_READY_TID", 3);
        KakaoPayOrderResponse pgResponse =
                new KakaoPayOrderResponse("T_READY_TID", "test-cid", "READY", null);
        given(kakaoPayService.order("T_READY_TID")).willReturn(pgResponse);

        // Act
        scheduler.reconcilePendingPayments();

        // Assert
        DeliveryOrder updated = deliveryOrderRepository.findById(order.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("PG 조회 실패 → 해당 건만 스킵, 나머지 계속 처리")
    void reconcile_pgQueryFails_skipsAndContinues() {
        // Arrange
        DeliveryOrder order1 = createPendingOrderWithTid("T_FAIL_TID", 3);
        DeliveryOrder order2 = createPendingOrderWithTid("T_SUCCESS_TID", 4);

        given(kakaoPayService.order("T_FAIL_TID"))
                .willThrow(new KakaoPayException("카카오페이 주문 조회에 실패했습니다."));
        KakaoPayOrderResponse pgResponse = new KakaoPayOrderResponse(
                "T_SUCCESS_TID",
                "test-cid",
                "SUCCESS_PAYMENT",
                List.of(new KakaoPayOrderResponse.PaymentActionDetail("A_OK", "PAYMENT")));
        given(kakaoPayService.order("T_SUCCESS_TID")).willReturn(pgResponse);

        // Act
        scheduler.reconcilePendingPayments();

        // Assert
        DeliveryOrder updated1 = deliveryOrderRepository.findById(order1.getId()).orElseThrow();
        assertThat(updated1.getStatus()).isEqualTo(DeliveryStatus.PENDING_PAYMENT);

        DeliveryOrder updated2 = deliveryOrderRepository.findById(order2.getId()).orElseThrow();
        assertThat(updated2.getStatus()).isEqualTo(DeliveryStatus.PAID);
    }

    @Test
    @DisplayName("KAKAOPAY disabled → PG 조회 안 함")
    void reconcile_kakaoPayDisabled_skips() {
        // Arrange
        given(kakaoPayConfigDto.isEnabled()).willReturn(false);
        createPendingOrderWithTid("T_DISABLED_TID", 3);

        // Act
        scheduler.reconcilePendingPayments();

        // Assert
        verify(kakaoPayService, never()).order(anyString());
    }
}
