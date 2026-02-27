package store.onuljang.worker.scheduler.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
import store.onuljang.shared.entity.enums.DeliveryStatus;
import store.onuljang.shared.entity.enums.ReservationStatus;
import store.onuljang.shared.util.TimeUtil;
import store.onuljang.shop.admin.entity.Admin;
import store.onuljang.shop.delivery.entity.DeliveryDailyAgg;
import store.onuljang.shop.delivery.entity.DeliveryOrder;
import store.onuljang.shop.delivery.repository.DeliveryDailyAggRepository;
import store.onuljang.shop.delivery.repository.DeliveryOrderRepository;
import store.onuljang.shop.product.entity.Product;
import store.onuljang.shop.reservation.entity.Reservation;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.support.TestFixture;

/**
 * 배달 주문 집계 스케줄러 테스트
 *
 * 배달 자동 완료 + 배달 일별 집계 검증
 */
@SpringBootTest
@ActiveProfiles("test")
@Import({TestS3Config.class, TestSqsConfig.class})
@Transactional
class DeliveryAggregationSchedulerTest {

    @Autowired
    private ReservationAggregationScheduler reservationAggregationScheduler;

    @Autowired
    private TestFixture testFixture;

    @Autowired
    private DeliveryDailyAggRepository deliveryDailyAggRepository;

    @Autowired
    private DeliveryOrderRepository deliveryOrderRepository;

    @Autowired
    private EntityManager em;

    private Admin admin;
    private Users user;

    @BeforeEach
    void setUp() {
        admin = testFixture.createDefaultAdmin();
        user = testFixture.createUser("배달테스트유저");
    }

    @Test
    @DisplayName("배달 주문 정산 - DELIVERED 배달 주문의 상품 금액과 배달비가 집계된다")
    void aggregate_DeliveredDeliveryOrders() {
        // Arrange
        LocalDate yesterday = TimeUtil.yesterdayDate();
        Product product =
                testFixture.createProduct("배달상품", 10, new BigDecimal("10000"), yesterday, admin);

        Reservation r1 =
                testFixture.createReservationWithStatus(user, product, 2, ReservationStatus.PICKED);
        Reservation r2 =
                testFixture.createReservationWithStatus(user, product, 3, ReservationStatus.PICKED);

        testFixture.createDeliveryOrderWithLink(user, r1, DeliveryStatus.DELIVERED);
        testFixture.createDeliveryOrderWithLink(user, r2, DeliveryStatus.DELIVERED);

        em.flush();
        em.clear();

        // Act
        reservationAggregationScheduler.aggregate();
        em.flush();
        em.clear();

        // Assert
        Optional<DeliveryDailyAgg> agg = deliveryDailyAggRepository.findAll().stream()
                .filter(a -> a.getSellDate().equals(yesterday))
                .findFirst();

        assertThat(agg).isPresent();
        assertThat(agg.get().getOrderCount()).isEqualTo(2);
        assertThat(agg.get().getQuantity()).isEqualTo(5);
        assertThat(agg.get().getAmount()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(agg.get().getDeliveryFee()).isEqualByComparingTo(new BigDecimal("5800"));
    }

    @Test
    @DisplayName("배달 주문이 없는 경우에도 정상 동작")
    void aggregate_NoDeliveryOrders() {
        // Act & Assert
        assertThatCode(() -> reservationAggregationScheduler.aggregate())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("미완료 배달 주문 자동 완료 - PAID 상태 배달 주문이 DELIVERED로 변경된다")
    void aggregate_AutoCompletesPaidDeliveryOrders() {
        // Arrange
        LocalDate yesterday = TimeUtil.yesterdayDate();
        Product product =
                testFixture.createProduct("배달상품", 10, new BigDecimal("10000"), yesterday, admin);
        Reservation r1 =
                testFixture.createReservationWithStatus(user, product, 2, ReservationStatus.PICKED);
        DeliveryOrder order =
                testFixture.createDeliveryOrderWithLink(user, r1, DeliveryStatus.PAID);
        Long orderId = order.getId();

        em.flush();
        em.clear();

        // Act
        reservationAggregationScheduler.aggregate();
        em.flush();
        em.clear();

        // Assert
        DeliveryOrder updated = deliveryOrderRepository.findById(orderId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);

        Optional<DeliveryDailyAgg> agg = deliveryDailyAggRepository.findAll().stream()
                .filter(a -> a.getSellDate().equals(yesterday))
                .findFirst();
        assertThat(agg).isPresent();
        assertThat(agg.get().getOrderCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("미완료 배달 주문 자동 완료 - OUT_FOR_DELIVERY 상태도 DELIVERED로 변경된다")
    void aggregate_AutoCompletesOutForDeliveryOrders() {
        // Arrange
        LocalDate yesterday = TimeUtil.yesterdayDate();
        Product product =
                testFixture.createProduct("배달상품", 10, new BigDecimal("15000"), yesterday, admin);
        Reservation r1 =
                testFixture.createReservationWithStatus(user, product, 1, ReservationStatus.PICKED);
        DeliveryOrder order = testFixture.createDeliveryOrderWithLink(
                user, r1, DeliveryStatus.OUT_FOR_DELIVERY);
        Long orderId = order.getId();

        em.flush();
        em.clear();

        // Act
        reservationAggregationScheduler.aggregate();
        em.flush();
        em.clear();

        // Assert
        DeliveryOrder updated = deliveryOrderRepository.findById(orderId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    @DisplayName("취소/실패 배달 주문은 자동 완료되지 않고 집계에서 제외된다")
    void aggregate_ExcludesCanceledAndFailedOrders() {
        // Arrange
        LocalDate yesterday = TimeUtil.yesterdayDate();
        Product product =
                testFixture.createProduct("배달상품", 10, new BigDecimal("10000"), yesterday, admin);

        Reservation r1 = testFixture.createReservationWithStatus(
                user, product, 2, ReservationStatus.CANCELED);
        Reservation r2 =
                testFixture.createReservationWithStatus(user, product, 1, ReservationStatus.PICKED);

        testFixture.createDeliveryOrderWithLink(user, r1, DeliveryStatus.CANCELED);
        testFixture.createDeliveryOrderWithLink(user, r2, DeliveryStatus.FAILED);

        em.flush();
        em.clear();

        // Act
        reservationAggregationScheduler.aggregate();
        em.flush();
        em.clear();

        // Assert
        List<DeliveryDailyAgg> aggs = deliveryDailyAggRepository.findAll();
        boolean hasYesterdayAgg =
                aggs.stream().anyMatch(a -> a.getSellDate().equals(yesterday));
        if (hasYesterdayAgg) {
            DeliveryDailyAgg agg = aggs.stream()
                    .filter(a -> a.getSellDate().equals(yesterday))
                    .findFirst()
                    .get();
            assertThat(agg.getOrderCount()).isEqualTo(0);
        }
    }

    @Test
    @DisplayName("하나의 배달 주문에 여러 예약이 연결된 경우 정확히 집계된다")
    void aggregate_MultipleReservationsInOneDeliveryOrder() {
        // Arrange
        LocalDate yesterday = TimeUtil.yesterdayDate();
        Product product1 =
                testFixture.createProduct("상품1", 10, new BigDecimal("10000"), yesterday, admin);
        Product product2 =
                testFixture.createProduct("상품2", 10, new BigDecimal("20000"), yesterday, admin);

        Reservation r1 = testFixture.createReservationWithStatus(
                user, product1, 2, ReservationStatus.PICKED);
        Reservation r2 = testFixture.createReservationWithStatus(
                user, product2, 1, ReservationStatus.PICKED);

        DeliveryOrder order =
                testFixture.createDeliveryOrderWithLink(user, r1, DeliveryStatus.DELIVERED);
        testFixture.linkReservationToDeliveryOrder(order, r2);

        em.flush();
        em.clear();

        // Act
        reservationAggregationScheduler.aggregate();
        em.flush();
        em.clear();

        // Assert
        Optional<DeliveryDailyAgg> agg = deliveryDailyAggRepository.findAll().stream()
                .filter(a -> a.getSellDate().equals(yesterday))
                .findFirst();

        assertThat(agg).isPresent();
        assertThat(agg.get().getOrderCount()).isEqualTo(1);
        assertThat(agg.get().getQuantity()).isEqualTo(3);
        assertThat(agg.get().getAmount()).isEqualByComparingTo(new BigDecimal("40000"));
        assertThat(agg.get().getDeliveryFee()).isEqualByComparingTo(new BigDecimal("2900"));
    }
}
