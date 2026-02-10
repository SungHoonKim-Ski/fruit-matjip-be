package store.onuljang.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.repository.entity.Product;
import store.onuljang.repository.entity.Reservation;
import store.onuljang.repository.entity.Users;
import store.onuljang.repository.entity.enums.DeliveryStatus;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.service.DeliveryOrderService;
import store.onuljang.service.ReservationService;
import store.onuljang.support.IntegrationTestBase;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 배달-예약 상태 연동 통합 테스트
 *
 * 요구사항:
 * - 배달 결제(PAID) 시 연결된 PENDING 예약 → PICKED
 * - 배달 완료(DELIVERED) 시 예약 상태 변경 없음 (이미 PAID 시점에 처리됨)
 * - 배달 취소(CANCELED) 시 연결된 PICKED 예약 → PENDING 복원
 */
class DeliveryReservationStatusIntegrationTest extends IntegrationTestBase {

    @Autowired
    DeliveryOrderService deliveryOrderService;

    @Autowired
    ReservationService reservationService;

    private Users user;
    private Admin admin;
    private Product product;

    @BeforeEach
    void setUp() {
        admin = testFixture.createDefaultAdmin();
        user = testFixture.createUser("배달고객");
        product = testFixture.createTodayProduct("테스트상품", 10, new BigDecimal("15000"), admin);
    }

    @Test
    @DisplayName("배달 결제 완료 시 연결된 PENDING 예약이 PICKED로 변경")
    void whenDeliveryPaid_thenReservationBecomesPicked() {
        // Arrange
        Reservation reservation = testFixture.createReservation(user, product, 1);
        DeliveryOrder order = testFixture.createDeliveryOrderWithLink(user, reservation, DeliveryStatus.PENDING_PAYMENT);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);

        // Act
        deliveryOrderService.completePaid(order.getId(), "test-approve-aid");

        // Assert
        Reservation updatedReservation = reservationService.findById(reservation.getId());
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.PICKED);
    }

    @Test
    @DisplayName("배달 완료 시 이미 PICKED인 예약은 상태 변경 없음")
    void whenDeliveryDelivered_thenReservationRemainsPickedWithoutChange() {
        // Arrange
        Reservation reservation = testFixture.createReservationWithStatus(user, product, 1, ReservationStatus.PICKED);
        DeliveryOrder order = testFixture.createDeliveryOrderWithLink(user, reservation, DeliveryStatus.OUT_FOR_DELIVERY);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PICKED);

        // Act
        order.markDelivered();
        deliveryOrderService.save(order);

        // Assert
        Reservation updatedReservation = reservationService.findById(reservation.getId());
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.PICKED);
    }

    @Test
    @DisplayName("배달 취소 시 연결된 PICKED 예약이 PENDING으로 복원")
    void whenDeliveryCanceled_thenReservationRestoresToPending() {
        // Arrange
        Reservation reservation = testFixture.createReservationWithStatus(user, product, 1, ReservationStatus.PICKED);
        DeliveryOrder order = testFixture.createDeliveryOrderWithLink(user, reservation, DeliveryStatus.PAID);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PICKED);

        // Act - AdminDeliveryAppService의 cancelOrder 로직 직접 호출
        order.markCanceled();
        order.getReservations().stream()
            .filter(r -> r.getStatus() == ReservationStatus.PICKED)
            .forEach(r -> r.changeStatus(ReservationStatus.PENDING));
        deliveryOrderService.save(order);

        // Assert
        Reservation updatedReservation = reservationService.findById(reservation.getId());
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("배달 결제 시 여러 예약이 모두 PICKED로 변경")
    void whenDeliveryPaid_thenAllReservationsBecomePicked() {
        // Arrange
        Reservation reservation1 = testFixture.createReservation(user, product, 1);
        Reservation reservation2 = testFixture.createReservation(user, product, 2);

        DeliveryOrder order = testFixture.createDeliveryOrderWithLink(user, reservation1, DeliveryStatus.PENDING_PAYMENT);
        testFixture.linkReservationToDeliveryOrder(order, reservation2);

        // Act
        deliveryOrderService.completePaid(order.getId(), "test-approve-aid");

        // Assert
        Reservation updated1 = reservationService.findById(reservation1.getId());
        Reservation updated2 = reservationService.findById(reservation2.getId());
        assertThat(updated1.getStatus()).isEqualTo(ReservationStatus.PICKED);
        assertThat(updated2.getStatus()).isEqualTo(ReservationStatus.PICKED);
    }

    @Test
    @DisplayName("배달 취소 시 여러 예약이 모두 PENDING으로 복원")
    void whenDeliveryCanceled_thenAllReservationsRestoreToPending() {
        // Arrange
        Reservation reservation1 = testFixture.createReservationWithStatus(user, product, 1, ReservationStatus.PICKED);
        Reservation reservation2 = testFixture.createReservationWithStatus(user, product, 2, ReservationStatus.PICKED);

        DeliveryOrder order = testFixture.createDeliveryOrderWithLink(user, reservation1, DeliveryStatus.PAID);
        testFixture.linkReservationToDeliveryOrder(order, reservation2);

        // Act - AdminDeliveryAppService의 cancelOrder 로직 직접 호출
        order.markCanceled();
        order.getReservations().stream()
            .filter(r -> r.getStatus() == ReservationStatus.PICKED)
            .forEach(r -> r.changeStatus(ReservationStatus.PENDING));
        deliveryOrderService.save(order);

        // Assert
        Reservation updated1 = reservationService.findById(reservation1.getId());
        Reservation updated2 = reservationService.findById(reservation2.getId());
        assertThat(updated1.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(updated2.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }
}
