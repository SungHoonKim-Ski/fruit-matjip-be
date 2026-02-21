package store.onuljang.unit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shop.admin.appservice.AdminDeliveryAppService;
import store.onuljang.config.TestS3Config;
import store.onuljang.shop.admin.entity.Admin;
import store.onuljang.shop.delivery.entity.DeliveryOrder;
import store.onuljang.shop.product.entity.Product;
import store.onuljang.shop.reservation.entity.Reservation;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.entity.enums.DeliveryStatus;
import store.onuljang.shared.entity.enums.ReservationStatus;
import store.onuljang.shop.delivery.service.DeliveryOrderService;
import store.onuljang.shop.reservation.service.ReservationService;
import store.onuljang.support.TestFixture;
import store.onuljang.shared.util.TimeUtil;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestS3Config.class)
@Transactional
class DeliveryAutoCompleteTest {

    @Autowired
    AdminDeliveryAppService adminDeliveryAppService;

    @Autowired
    DeliveryOrderService deliveryOrderService;

    @Autowired
    ReservationService reservationService;

    @Autowired
    TestFixture testFixture;

    private Users user;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        ZonedDateTime fixed = ZonedDateTime.of(
            LocalDate.of(2026, 2, 7), LocalTime.of(15, 0), TimeUtil.KST);
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
    @DisplayName("OUT_FOR_DELIVERY 상태이고 acceptedAt + 90분이 지난 주문은 자동 완료 대상")
    void processAutoCompleteDelivery_outForDeliveryAndExpired_marksDelivered() {
        // given
        ZonedDateTime acceptTime = ZonedDateTime.of(
            LocalDate.of(2026, 2, 7), LocalTime.of(13, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(acceptTime.toInstant(), TimeUtil.KST));

        DeliveryOrder order = testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PAID);
        adminDeliveryAppService.accept(order.getId(), 30);

        // when - 현재 시각 15:00 (acceptedAt 13:00 + 120분 경과)
        ZonedDateTime nowTime = ZonedDateTime.of(
            LocalDate.of(2026, 2, 7), LocalTime.of(15, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(nowTime.toInstant(), TimeUtil.KST));

        LocalDateTime cutoff = TimeUtil.nowDateTime().minusMinutes(90);
        long updated = adminDeliveryAppService.processAutoCompleteDelivery(cutoff);

        // then
        assertThat(updated).isEqualTo(1);
        DeliveryOrder result = deliveryOrderService.findById(order.getId());
        assertThat(result.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    @DisplayName("OUT_FOR_DELIVERY 상태이지만 acceptedAt + 90분이 안 지난 주문은 대상이 아님")
    void processAutoCompleteDelivery_outForDeliveryButNotExpired_doesNotMark() {
        // given
        ZonedDateTime acceptTime = ZonedDateTime.of(
            LocalDate.of(2026, 2, 7), LocalTime.of(14, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(acceptTime.toInstant(), TimeUtil.KST));

        DeliveryOrder order = testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PAID);
        adminDeliveryAppService.accept(order.getId(), 30);

        // when - 현재 시각 15:00 (acceptedAt 14:00 + 60분 경과, 90분 미만)
        ZonedDateTime nowTime = ZonedDateTime.of(
            LocalDate.of(2026, 2, 7), LocalTime.of(15, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(nowTime.toInstant(), TimeUtil.KST));

        LocalDateTime cutoff = TimeUtil.nowDateTime().minusMinutes(90);
        long updated = adminDeliveryAppService.processAutoCompleteDelivery(cutoff);

        // then
        assertThat(updated).isEqualTo(0);
        DeliveryOrder result = deliveryOrderService.findById(order.getId());
        assertThat(result.getStatus()).isEqualTo(DeliveryStatus.OUT_FOR_DELIVERY);
    }

    @Test
    @DisplayName("PAID 상태인 주문은 자동 완료 대상이 아님")
    void processAutoCompleteDelivery_paidStatus_doesNotMark() {
        // given
        DeliveryOrder order = testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PAID);

        // when
        LocalDateTime cutoff = TimeUtil.nowDateTime().minusMinutes(90);
        long updated = adminDeliveryAppService.processAutoCompleteDelivery(cutoff);

        // then
        assertThat(updated).isEqualTo(0);
        DeliveryOrder result = deliveryOrderService.findById(order.getId());
        assertThat(result.getStatus()).isEqualTo(DeliveryStatus.PAID);
    }

    @Test
    @DisplayName("이미 DELIVERED 상태인 주문은 자동 완료 대상이 아님")
    void processAutoCompleteDelivery_alreadyDelivered_doesNotMark() {
        // given
        ZonedDateTime acceptTime = ZonedDateTime.of(
            LocalDate.of(2026, 2, 7), LocalTime.of(13, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(acceptTime.toInstant(), TimeUtil.KST));

        DeliveryOrder order = testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PAID);
        adminDeliveryAppService.accept(order.getId(), 30);
        adminDeliveryAppService.updateStatus(order.getId(), DeliveryStatus.DELIVERED);

        // when - 현재 시각 15:00
        ZonedDateTime nowTime = ZonedDateTime.of(
            LocalDate.of(2026, 2, 7), LocalTime.of(15, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(nowTime.toInstant(), TimeUtil.KST));

        LocalDateTime cutoff = TimeUtil.nowDateTime().minusMinutes(90);
        long updated = adminDeliveryAppService.processAutoCompleteDelivery(cutoff);

        // then
        assertThat(updated).isEqualTo(0);
        DeliveryOrder result = deliveryOrderService.findById(order.getId());
        assertThat(result.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    @DisplayName("여러 주문 중 조건에 맞는 주문만 자동 완료")
    void processAutoCompleteDelivery_multipleOrders_marksOnlyExpired() {
        // given
        Admin admin = testFixture.createAdmin("관리자2", "admin2@test.com", "password");
        Users user2 = testFixture.createUser("배달고객2");
        Product product2 = testFixture.createTodayProduct("사과", 10, new BigDecimal("20000"), admin);
        Reservation reservation2 = testFixture.createReservation(user2, product2, 1);

        // 주문1: acceptedAt 13:00 (90분 이상 경과)
        ZonedDateTime accept1 = ZonedDateTime.of(
            LocalDate.of(2026, 2, 7), LocalTime.of(13, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(accept1.toInstant(), TimeUtil.KST));
        DeliveryOrder order1 = testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PAID);
        adminDeliveryAppService.accept(order1.getId(), 30);

        // 주문2: acceptedAt 14:00 (90분 미만)
        ZonedDateTime accept2 = ZonedDateTime.of(
            LocalDate.of(2026, 2, 7), LocalTime.of(14, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(accept2.toInstant(), TimeUtil.KST));
        DeliveryOrder order2 = testFixture.createDeliveryOrder(user2, reservation2, DeliveryStatus.PAID);
        adminDeliveryAppService.accept(order2.getId(), 30);

        // when - 현재 시각 15:00
        ZonedDateTime nowTime = ZonedDateTime.of(
            LocalDate.of(2026, 2, 7), LocalTime.of(15, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(nowTime.toInstant(), TimeUtil.KST));

        LocalDateTime cutoff = TimeUtil.nowDateTime().minusMinutes(90);
        long updated = adminDeliveryAppService.processAutoCompleteDelivery(cutoff);

        // then
        assertThat(updated).isEqualTo(1);

        DeliveryOrder result1 = deliveryOrderService.findById(order1.getId());
        assertThat(result1.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);

        DeliveryOrder result2 = deliveryOrderService.findById(order2.getId());
        assertThat(result2.getStatus()).isEqualTo(DeliveryStatus.OUT_FOR_DELIVERY);
    }

    @Test
    @DisplayName("자동 배달 완료 시 예약 상태 변경 없음 (이미 PAID 시점에 PICKED 처리됨)")
    void processAutoCompleteDelivery_noReservationStatusChange() {
        // given
        ZonedDateTime acceptTime = ZonedDateTime.of(
            LocalDate.of(2026, 2, 7), LocalTime.of(13, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(acceptTime.toInstant(), TimeUtil.KST));

        // 예약은 이미 PAID 시점에 PICKED로 변경되었음
        reservation.changeStatus(ReservationStatus.PICKED);
        DeliveryOrder order = testFixture.createDeliveryOrderWithLink(user, reservation, DeliveryStatus.PAID);
        adminDeliveryAppService.accept(order.getId(), 30);

        // when - 현재 시각 15:00 (acceptedAt 13:00 + 120분 경과)
        ZonedDateTime nowTime = ZonedDateTime.of(
            LocalDate.of(2026, 2, 7), LocalTime.of(15, 0), TimeUtil.KST);
        TimeUtil.setClock(Clock.fixed(nowTime.toInstant(), TimeUtil.KST));

        LocalDateTime cutoff = TimeUtil.nowDateTime().minusMinutes(90);
        long updated = adminDeliveryAppService.processAutoCompleteDelivery(cutoff);

        // then
        assertThat(updated).isEqualTo(1);

        DeliveryOrder result = deliveryOrderService.findById(order.getId());
        assertThat(result.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);

        // 자동 완료 시점에는 예약 상태 변경이 없음 (이미 PICKED 상태 유지)
        Reservation updatedReservation = reservationService.findById(reservation.getId());
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.PICKED);
    }
}
