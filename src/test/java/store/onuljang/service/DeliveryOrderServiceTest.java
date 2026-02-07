package store.onuljang.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
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

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
    "KAKAOPAY.ENABLED=false",
    "KAKAOPAY.SECRET_KEY=test-secret-key",
    "KAKAOPAY.CID=test-cid",
    "KAKAOPAY.APPROVAL_URL=http://localhost/approve",
    "KAKAOPAY.CANCEL_URL=http://localhost/cancel",
    "KAKAOPAY.FAIL_URL=http://localhost/fail",
    "KAKAOPAY.HOST=https://open-api.kakaopay.com"
})
class DeliveryOrderServiceTest extends IntegrationTestBase {

    @Autowired
    DeliveryOrderService deliveryOrderService;

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
    void completePaid_pendingOrder_marksPaid() {
        // Arrange
        Users user = testFixture.createUser("결제고객");
        Product product = testFixture.createTodayProduct("딸기", 5, new BigDecimal("15000"),
            testFixture.createDefaultAdmin());
        Reservation reservation = testFixture.createReservation(user, product, 1);
        DeliveryOrder order = testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PENDING_PAYMENT);

        // Act
        deliveryOrderService.completePaid(order.getId(), "A123456");

        // Assert
        DeliveryOrder updated = deliveryOrderService.findById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.PAID);
        assertThat(updated.getPaidAt()).isNotNull();
    }

    @Test
    void completePaid_alreadyPaidOrder_doesNothing() {
        // Arrange
        Users user = testFixture.createUser("결제고객2");
        Product product = testFixture.createTodayProduct("사과", 5, new BigDecimal("15000"),
            testFixture.createDefaultAdmin());
        Reservation reservation = testFixture.createReservation(user, product, 1);
        DeliveryOrder order = testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PAID);

        // Act — should not throw, just return
        deliveryOrderService.completePaid(order.getId(), "A789");

        // Assert — status unchanged
        DeliveryOrder updated = deliveryOrderService.findById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.PAID);
    }
}
