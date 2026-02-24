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
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.config.TestS3Config;
import store.onuljang.config.TestSqsConfig;
import store.onuljang.shop.delivery.scheduler.DeliveryPaymentExpireScheduler;
import store.onuljang.shop.delivery.repository.DeliveryOrderRepository;
import store.onuljang.shop.admin.entity.Admin;
import store.onuljang.shop.delivery.entity.DeliveryOrder;
import store.onuljang.shop.product.entity.Product;
import store.onuljang.shop.reservation.entity.Reservation;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.entity.enums.DeliveryStatus;
import store.onuljang.support.TestFixture;
import store.onuljang.shared.util.TimeUtil;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestS3Config.class, TestSqsConfig.class})
@Transactional
class DeliveryPaymentExpireSchedulerTest {

    @Autowired
    DeliveryPaymentExpireScheduler scheduler;

    @Autowired
    DeliveryOrderRepository deliveryOrderRepository;

    @Autowired
    TestFixture testFixture;

    @Autowired
    EntityManager entityManager;

    private Users user;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        ZonedDateTime fixed = ZonedDateTime.of(
            LocalDate.of(2026, 1, 21), LocalTime.of(10, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(fixed.toInstant(), TimeUtil.KST));

        Admin admin = testFixture.createDefaultAdmin();
        user = testFixture.createUser("배달고객");
        Product product = testFixture.createTodayProduct("딸기", 10, new BigDecimal("15000"), admin);
        reservation = testFixture.createReservation(user, product, 1);
    }

    @AfterEach
    void tearDown() {
        TimeUtil.resetClock();
    }

    @Test
    @DisplayName("5분 초과 PENDING_PAYMENT 주문은 FAILED로 변경")
    void expirePendingPayments_expiredOrder_markedFailed() {
        // Arrange
        DeliveryOrder order = testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PENDING_PAYMENT);
        order.updateCreatedAt(TimeUtil.nowDateTime().minusMinutes(10));
        deliveryOrderRepository.saveAndFlush(order);
        entityManager.clear();

        // Act
        scheduler.expirePendingPayments();

        // Assert - 스케줄러가 로드한 managed entity를 통해 확인
        DeliveryOrder updated = deliveryOrderRepository.findById(order.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.FAILED);
    }

    @Test
    @DisplayName("5분 이내 PENDING_PAYMENT 주문은 변경되지 않음")
    void expirePendingPayments_recentOrder_notAffected() {
        // Arrange
        DeliveryOrder order = testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PENDING_PAYMENT);
        deliveryOrderRepository.saveAndFlush(order);
        entityManager.clear();

        // Act
        scheduler.expirePendingPayments();

        // Assert
        DeliveryOrder updated = deliveryOrderRepository.findById(order.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("PAID 주문은 스케줄러 영향 없음")
    void expirePendingPayments_paidOrder_notAffected() {
        // Arrange
        DeliveryOrder order = testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PAID);
        order.updateCreatedAt(TimeUtil.nowDateTime().minusMinutes(10));
        deliveryOrderRepository.saveAndFlush(order);
        entityManager.clear();

        // Act
        scheduler.expirePendingPayments();

        // Assert
        DeliveryOrder updated = deliveryOrderRepository.findById(order.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.PAID);
    }

    @Test
    @DisplayName("주문이 없는 경우에도 정상 동작")
    void expirePendingPayments_noOrders_noException() {
        // Act & Assert
        scheduler.expirePendingPayments();
    }
}
