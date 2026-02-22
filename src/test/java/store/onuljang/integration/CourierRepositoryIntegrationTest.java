package store.onuljang.integration;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierOrderItem;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.repository.CourierOrderRepository;
import store.onuljang.courier.repository.CourierProductRepository;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.support.IntegrationTestBase;

@DisplayName("Courier 레포지토리 통합 테스트")
class CourierRepositoryIntegrationTest extends IntegrationTestBase {

    @Autowired
    private CourierProductRepository courierProductRepository;

    @Autowired
    private CourierOrderRepository courierOrderRepository;

    @Autowired
    private EntityManager entityManager;

    private Users user;

    @BeforeEach
    void setUp() {
        user = testFixture.createUser("테스트");
    }

    // --- helper methods ---

    private CourierProduct saveCourierProduct(String name, int sortOrder) {
        CourierProduct product = CourierProduct.builder()
                .name(name)
                .productUrl("https://example.com/image.jpg")
                .price(new BigDecimal("10000"))
                .stock(100)
                .sortOrder(sortOrder)
                .build();
        return courierProductRepository.save(product);
    }

    private CourierOrder savePaidOrder(Users user, LocalDateTime paidAt) {
        CourierOrder order = CourierOrder.builder()
                .user(user)
                .displayCode("C-" + UUID.randomUUID().toString().substring(0, 12))
                .status(CourierOrderStatus.PAID)
                .receiverName("홍길동")
                .receiverPhone("010-1234-5678")
                .postalCode("06134")
                .address1("서울시 강남구 테헤란로 1")
                .productAmount(new BigDecimal("10000"))
                .shippingFee(new BigDecimal("3000"))
                .totalAmount(new BigDecimal("13000"))
                .build();
        CourierOrder saved = courierOrderRepository.save(order);
        // paidAt은 markPaid()가 TimeUtil.nowDateTime()을 사용하므로 리플렉션으로 직접 설정
        org.springframework.test.util.ReflectionTestUtils.setField(saved, "paidAt", paidAt);
        return courierOrderRepository.save(saved);
    }

    private CourierOrderItem saveOrderItem(CourierOrder order, CourierProduct product) {
        CourierOrderItem item = CourierOrderItem.builder()
                .courierOrder(order)
                .courierProduct(product)
                .productName(product.getName())
                .productPrice(product.getPrice())
                .quantity(1)
                .amount(product.getPrice())
                .build();
        entityManager.persist(item);
        return item;
    }

    @Nested
    @DisplayName("CourierProductRepository.findMinSortOrder")
    class FindMinSortOrder {

        @Test
        @DisplayName("상품이 없으면 COALESCE로 0을 반환한다")
        void 상품_없음_0_반환() {
            // Arrange — 테스트 격리: 현재 트랜잭션 내 상품 없음

            // Act
            int result = courierProductRepository.findMinSortOrder();

            // Assert
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("여러 상품 중 가장 작은 sortOrder를 반환한다 (음수 포함)")
        void 음수_포함_최솟값_반환() {
            // Arrange
            saveCourierProduct("상품A", -5);
            saveCourierProduct("상품B", 0);
            saveCourierProduct("상품C", 3);
            entityManager.flush();

            // Act
            int result = courierProductRepository.findMinSortOrder();

            // Assert
            assertThat(result).isEqualTo(-5);
        }
    }

    @Nested
    @DisplayName("CourierOrderRepository.findByDateRangeAndStatuses")
    class FindByDateRangeAndStatuses {

        @Test
        @DisplayName("날짜 범위 내 PAID 주문만 반환된다")
        void 날짜_범위_내_주문_반환() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품", 0);
            LocalDateTime base = LocalDateTime.of(2025, 6, 15, 12, 0, 0);

            CourierOrder inRangeOrder = savePaidOrder(user, base);
            saveOrderItem(inRangeOrder, product);

            CourierOrder outOfRangeOrder = savePaidOrder(user, base.minusDays(2));
            saveOrderItem(outOfRangeOrder, product);

            entityManager.flush();
            entityManager.clear();

            LocalDateTime startDateTime = base.minusDays(1);
            LocalDateTime endDateTime = base.plusDays(1);

            // Act
            List<CourierOrder> result = courierOrderRepository.findByDateRangeAndStatuses(
                    startDateTime, endDateTime, List.of(CourierOrderStatus.PAID));

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(inRangeOrder.getId());
        }

        @Test
        @DisplayName("범위 바깥 주문은 결과에 포함되지 않는다")
        void 범위_밖_주문_미포함() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품2", 0);
            LocalDateTime base = LocalDateTime.of(2025, 7, 1, 0, 0, 0);

            CourierOrder outOfRangeOrder = savePaidOrder(user, base.minusDays(10));
            saveOrderItem(outOfRangeOrder, product);

            entityManager.flush();
            entityManager.clear();

            LocalDateTime startDateTime = base;
            LocalDateTime endDateTime = base.plusDays(1);

            // Act
            List<CourierOrder> result = courierOrderRepository.findByDateRangeAndStatuses(
                    startDateTime, endDateTime, List.of(CourierOrderStatus.PAID));

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("상태 필터에 맞지 않는 주문은 포함되지 않는다")
        void 상태_불일치_주문_미포함() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품3", 0);
            LocalDateTime base = LocalDateTime.of(2025, 8, 1, 10, 0, 0);

            CourierOrder canceledOrder = CourierOrder.builder()
                    .user(user)
                    .displayCode("C-" + UUID.randomUUID().toString().substring(0, 12))
                    .status(CourierOrderStatus.CANCELED)
                    .receiverName("홍길동")
                    .receiverPhone("010-1234-5678")
                    .postalCode("06134")
                    .address1("서울시 강남구")
                    .productAmount(new BigDecimal("10000"))
                    .shippingFee(new BigDecimal("3000"))
                    .totalAmount(new BigDecimal("13000"))
                    .build();
            CourierOrder saved = courierOrderRepository.save(canceledOrder);
            org.springframework.test.util.ReflectionTestUtils.setField(saved, "paidAt", base);
            courierOrderRepository.save(saved);
            saveOrderItem(saved, product);

            entityManager.flush();
            entityManager.clear();

            // Act
            List<CourierOrder> result = courierOrderRepository.findByDateRangeAndStatuses(
                    base.minusHours(1), base.plusHours(1), List.of(CourierOrderStatus.PAID));

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("CourierOrderRepository.findByDateRangeAndStatusesAndProduct")
    class FindByDateRangeAndStatusesAndProduct {

        @Test
        @DisplayName("특정 상품 ID를 포함한 주문만 반환된다")
        void 특정_상품_주문_반환() {
            // Arrange
            CourierProduct productA = saveCourierProduct("상품A", 0);
            CourierProduct productB = saveCourierProduct("상품B", 1);
            LocalDateTime base = LocalDateTime.of(2025, 9, 1, 12, 0, 0);

            CourierOrder orderWithA = savePaidOrder(user, base);
            saveOrderItem(orderWithA, productA);

            CourierOrder orderWithB = savePaidOrder(user, base);
            saveOrderItem(orderWithB, productB);

            entityManager.flush();
            entityManager.clear();

            // Act
            List<CourierOrder> result = courierOrderRepository.findByDateRangeAndStatusesAndProduct(
                    base.minusHours(1), base.plusHours(1),
                    List.of(CourierOrderStatus.PAID),
                    productA.getId());

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(orderWithA.getId());
        }

        @Test
        @DisplayName("해당 상품이 없는 경우 빈 목록을 반환한다")
        void 해당_상품_없음_빈_목록() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품X", 0);
            LocalDateTime base = LocalDateTime.of(2025, 10, 1, 12, 0, 0);

            CourierOrder order = savePaidOrder(user, base);
            saveOrderItem(order, product);

            entityManager.flush();
            entityManager.clear();

            long nonExistentProductId = -999L;

            // Act
            List<CourierOrder> result = courierOrderRepository.findByDateRangeAndStatusesAndProduct(
                    base.minusHours(1), base.plusHours(1),
                    List.of(CourierOrderStatus.PAID),
                    nonExistentProductId);

            // Assert
            assertThat(result).isEmpty();
        }
    }
}
