package store.onuljang.appservice;

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
import store.onuljang.config.TestS3Config;
import store.onuljang.exception.AdminValidateException;
import store.onuljang.repository.entity.*;
import store.onuljang.repository.entity.enums.DeliveryStatus;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.service.DeliveryOrderService;
import store.onuljang.service.ReservationService;
import store.onuljang.service.KakaoPayService;
import store.onuljang.support.TestFixture;
import store.onuljang.util.TimeUtil;

import java.math.BigDecimal;
import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestS3Config.class)
@Transactional
class AdminDeliveryAppServiceTest {

    @Autowired
    AdminDeliveryAppService adminDeliveryAppService;

    @Autowired
    DeliveryOrderService deliveryOrderService;

    @Autowired
    ReservationService reservationService;

    @Autowired
    TestFixture testFixture;

    @MockitoBean
    KakaoPayService kakaoPayService;

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

    // --- OUT_FOR_DELIVERY ---

    @Test
    @DisplayName("PAID -> OUT_FOR_DELIVERY 상태 변경 성공")
    void updateStatus_paidToOutForDelivery_success() {
        // given
        DeliveryOrder order = testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PAID);

        // when
        adminDeliveryAppService.updateStatus(order.getId(), DeliveryStatus.OUT_FOR_DELIVERY);

        // then
        DeliveryOrder updated = deliveryOrderService.findById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.OUT_FOR_DELIVERY);
    }

    @Test
    @DisplayName("PENDING_PAYMENT -> OUT_FOR_DELIVERY 변경 시 예외")
    void updateStatus_pendingToOutForDelivery_throwsException() {
        // given
        DeliveryOrder order = testFixture.createDeliveryOrder(
            user, reservation, DeliveryStatus.PENDING_PAYMENT);

        // when / then
        assertThatThrownBy(() ->
            adminDeliveryAppService.updateStatus(order.getId(), DeliveryStatus.OUT_FOR_DELIVERY))
            .isInstanceOf(AdminValidateException.class)
            .hasMessageContaining("결제 완료 상태에서만");
    }

    // --- DELIVERED ---

    @Test
    @DisplayName("OUT_FOR_DELIVERY -> DELIVERED 상태 변경 성공")
    void updateStatus_outForDeliveryToDelivered_success() {
        // given
        // 예약은 이미 PAID 시점에 PICKED로 변경되었음
        reservation.changeStatus(ReservationStatus.PICKED);
        DeliveryOrder order = testFixture.createDeliveryOrderWithLink(
            user, reservation, DeliveryStatus.OUT_FOR_DELIVERY);

        // when
        adminDeliveryAppService.updateStatus(order.getId(), DeliveryStatus.DELIVERED);

        // then
        DeliveryOrder updated = deliveryOrderService.findById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);

        // DELIVERED 시점에는 예약 상태 변경이 없음 (이미 PICKED 상태 유지)
        Reservation updatedReservation = reservationService.findById(reservation.getId());
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.PICKED);
    }

    @Test
    @DisplayName("PAID -> DELIVERED 변경 시 예외")
    void updateStatus_paidToDelivered_throwsException() {
        // given
        DeliveryOrder order = testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PAID);

        // when / then
        assertThatThrownBy(() ->
            adminDeliveryAppService.updateStatus(order.getId(), DeliveryStatus.DELIVERED))
            .isInstanceOf(AdminValidateException.class)
            .hasMessageContaining("배달중 상태에서만");
    }

    // --- CANCELED ---

    @Test
    @DisplayName("PENDING_PAYMENT -> CANCELED 관리자 취소 성공")
    void updateStatus_pendingPaymentToCanceled_success() {
        // given
        DeliveryOrder order = testFixture.createDeliveryOrder(
            user, reservation, DeliveryStatus.PENDING_PAYMENT);

        // when
        adminDeliveryAppService.updateStatus(order.getId(), DeliveryStatus.CANCELED);

        // then
        DeliveryOrder updated = deliveryOrderService.findById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.CANCELED);
    }

    @Test
    @DisplayName("PAID -> CANCELED 관리자 취소 성공")
    void updateStatus_paidToCanceled_success() {
        // given
        DeliveryOrder order = testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PAID);

        // when
        adminDeliveryAppService.updateStatus(order.getId(), DeliveryStatus.CANCELED);

        // then
        DeliveryOrder updated = deliveryOrderService.findById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.CANCELED);
    }

    @Test
    @DisplayName("OUT_FOR_DELIVERY -> CANCELED 관리자 취소 성공")
    void updateStatus_outForDeliveryToCanceled_success() {
        // given
        DeliveryOrder order = testFixture.createDeliveryOrder(
            user, reservation, DeliveryStatus.OUT_FOR_DELIVERY);

        // when
        adminDeliveryAppService.updateStatus(order.getId(), DeliveryStatus.CANCELED);

        // then
        DeliveryOrder updated = deliveryOrderService.findById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.CANCELED);
    }

    @Test
    @DisplayName("DELIVERED -> CANCELED 변경 시 예외")
    void updateStatus_deliveredToCanceled_throwsException() {
        // given
        DeliveryOrder order = testFixture.createDeliveryOrder(
            user, reservation, DeliveryStatus.DELIVERED);

        // when / then
        assertThatThrownBy(() ->
            adminDeliveryAppService.updateStatus(order.getId(), DeliveryStatus.CANCELED))
            .isInstanceOf(AdminValidateException.class)
            .hasMessageContaining("배달 완료된 주문은 취소");
    }

    // --- invalid transitions ---

    @Test
    @DisplayName("PAID 상태로 변경 시 예외")
    void updateStatus_toPaid_throwsException() {
        // given
        DeliveryOrder order = testFixture.createDeliveryOrder(
            user, reservation, DeliveryStatus.PENDING_PAYMENT);

        // when / then
        assertThatThrownBy(() ->
            adminDeliveryAppService.updateStatus(order.getId(), DeliveryStatus.PAID))
            .isInstanceOf(AdminValidateException.class)
            .hasMessageContaining("변경할 수 없는 상태");
    }

    @Test
    @DisplayName("FAILED 상태로 변경 시 예외")
    void updateStatus_toFailed_throwsException() {
        // given
        DeliveryOrder order = testFixture.createDeliveryOrder(
            user, reservation, DeliveryStatus.PENDING_PAYMENT);

        // when / then
        assertThatThrownBy(() ->
            adminDeliveryAppService.updateStatus(order.getId(), DeliveryStatus.FAILED))
            .isInstanceOf(AdminValidateException.class)
            .hasMessageContaining("변경할 수 없는 상태");
    }

    // --- accept ---

    @Test
    @DisplayName("PAID 상태에서 접수 성공 - estimatedMinutes와 acceptedAt 설정")
    void accept_paid_success() {
        // given
        DeliveryOrder order = testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PAID);

        // when
        adminDeliveryAppService.accept(order.getId(), 30);

        // then
        DeliveryOrder updated = deliveryOrderService.findById(order.getId());
        assertThat(updated.getEstimatedMinutes()).isEqualTo(30);
        assertThat(updated.getAcceptedAt()).isEqualTo(TimeUtil.nowDateTime());
    }

    @Test
    @DisplayName("PENDING_PAYMENT 상태에서 접수 시 예외")
    void accept_pendingPayment_throwsException() {
        // given
        DeliveryOrder order = testFixture.createDeliveryOrder(
            user, reservation, DeliveryStatus.PENDING_PAYMENT);

        // when / then
        assertThatThrownBy(() ->
            adminDeliveryAppService.accept(order.getId(), 30))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("PAID 상태에서만 접수");
    }

    @Test
    @DisplayName("이미 접수된 주문에 다시 접수 시 상태가 유지되므로 예외 없음")
    void accept_alreadyAccepted_updatesAgain() {
        // given
        DeliveryOrder order = testFixture.createDeliveryOrder(user, reservation, DeliveryStatus.PAID);
        adminDeliveryAppService.accept(order.getId(), 30);

        // when
        adminDeliveryAppService.accept(order.getId(), 40);

        // then
        DeliveryOrder updated = deliveryOrderService.findById(order.getId());
        assertThat(updated.getEstimatedMinutes()).isEqualTo(40);
    }

    @Test
    @DisplayName("OUT_FOR_DELIVERY 상태에서 접수 시 예상 시간 업데이트 가능")
    void accept_outForDelivery_updatesEstimatedTime() {
        // given
        DeliveryOrder order = testFixture.createDeliveryOrder(
            user, reservation, DeliveryStatus.OUT_FOR_DELIVERY);

        // when
        adminDeliveryAppService.accept(order.getId(), 30);

        // then
        DeliveryOrder updated = deliveryOrderService.findById(order.getId());
        assertThat(updated.getEstimatedMinutes()).isEqualTo(30);
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.OUT_FOR_DELIVERY);
    }

    @Test
    @DisplayName("배달 완료 시 예약 상태 변경 없음 (이미 PAID 시점에 PICKED 처리됨)")
    void updateStatus_delivered_noReservationStatusChange() {
        // given
        Admin admin = testFixture.createDefaultAdmin();
        Product product = testFixture.createTodayProduct("사과", 10, new BigDecimal("20000"), admin);
        // 예약은 이미 PAID 시점에 PICKED로 변경되었음
        Reservation reservation1 = testFixture.createReservationWithStatus(user, product, 1, ReservationStatus.PICKED);

        DeliveryOrder order = testFixture.createDeliveryOrderWithLink(user, reservation1, DeliveryStatus.OUT_FOR_DELIVERY);

        // when
        adminDeliveryAppService.updateStatus(order.getId(), DeliveryStatus.DELIVERED);

        // then - DELIVERED 시점에는 예약 상태 변경이 없음
        Reservation updated1 = reservationService.findById(reservation1.getId());
        assertThat(updated1.getStatus()).isEqualTo(ReservationStatus.PICKED);
    }
}
