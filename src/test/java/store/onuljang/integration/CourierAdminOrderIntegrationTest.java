package store.onuljang.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import store.onuljang.courier.appservice.CourierAdminOrderAppService;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierOrderItem;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.repository.CourierOrderRepository;
import store.onuljang.courier.repository.CourierProductRepository;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.entity.enums.UserPointTransactionType;
import store.onuljang.shared.exception.AdminValidateException;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.user.service.UserPointService;
import store.onuljang.support.IntegrationTestBase;

@DisplayName("관리자 택배 주문 관리 통합 테스트")
class CourierAdminOrderIntegrationTest extends IntegrationTestBase {

    @Autowired
    private CourierAdminOrderAppService courierAdminOrderAppService;

    @Autowired
    private UserPointService userPointService;

    @Autowired
    private CourierOrderRepository courierOrderRepository;

    @Autowired
    private CourierProductRepository courierProductRepository;

    @Autowired
    private EntityManager entityManager;

    private Users user;

    @BeforeEach
    void setUp() {
        user = testFixture.createUser("관리자주문테스트");
    }

    // --- helper methods ---

    private CourierProduct saveCourierProduct(String name) {
        CourierProduct product = CourierProduct.builder()
                .name(name)
                .productUrl("https://example.com/image.jpg")
                .price(new BigDecimal("10000"))
                .sortOrder(0)
                .build();
        return courierProductRepository.save(product);
    }

    private CourierOrder saveCourierOrder(Users user, CourierOrderStatus status) {
        return saveCourierOrder(user, status, BigDecimal.ZERO);
    }

    private CourierOrder saveCourierOrder(Users user, CourierOrderStatus status, BigDecimal pointUsed) {
        BigDecimal totalAmount = new BigDecimal("13000");
        CourierOrder order = CourierOrder.builder()
                .user(user)
                .displayCode("C-" + UUID.randomUUID().toString().substring(0, 12))
                .status(status)
                .receiverName("홍길동")
                .receiverPhone("010-1234-5678")
                .postalCode("06134")
                .address1("서울시 강남구 테헤란로 1")
                .productAmount(new BigDecimal("10000"))
                .shippingFee(new BigDecimal("3000"))
                .totalAmount(totalAmount)
                .pointUsed(pointUsed)
                .build();
        return courierOrderRepository.save(order);
    }

    private void addOrderItem(CourierOrder order, CourierProduct product) {
        CourierOrderItem item = CourierOrderItem.builder()
                .courierOrder(order)
                .courierProduct(product)
                .productName(product.getName())
                .productPrice(product.getPrice())
                .quantity(1)
                .amount(product.getPrice())
                .build();
        entityManager.persist(item);
        order.getItems().add(item);
    }

    private void givePoints(Users user, BigDecimal amount) {
        userPointService.earn(user.getUid(), amount,
                UserPointTransactionType.EARN_ADMIN, "테스트 포인트 지급", "TEST", null, "test");
    }

    @Nested
    @DisplayName("상태 전이 테스트 - updateStatus()")
    class StatusTransitionTests {

        @Test
        @DisplayName("PAID → PREPARING 전환 성공")
        void PAID_에서_PREPARING_전환_성공() {
            // Arrange
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.PAID);
            entityManager.flush();

            // Act
            courierAdminOrderAppService.updateStatus(order.getId(), CourierOrderStatus.PREPARING);

            // Assert
            entityManager.flush();
            entityManager.clear();
            CourierOrder updated = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(CourierOrderStatus.PREPARING);
        }

        @Test
        @DisplayName("PAID → SHIPPED 전환 성공 (PREPARING 건너뛰기 가능)")
        void PAID_에서_SHIPPED_건너뛰기_성공() {
            // Arrange
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.PAID);
            entityManager.flush();

            // Act
            courierAdminOrderAppService.updateStatus(order.getId(), CourierOrderStatus.SHIPPED);

            // Assert
            entityManager.flush();
            entityManager.clear();
            CourierOrder updated = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(CourierOrderStatus.SHIPPED);
        }

        @Test
        @DisplayName("PREPARING → SHIPPED 전환 성공")
        void PREPARING_에서_SHIPPED_전환_성공() {
            // Arrange
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.PREPARING);
            entityManager.flush();

            // Act
            courierAdminOrderAppService.updateStatus(order.getId(), CourierOrderStatus.SHIPPED);

            // Assert
            entityManager.flush();
            entityManager.clear();
            CourierOrder updated = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(CourierOrderStatus.SHIPPED);
        }

        @Test
        @DisplayName("SHIPPED → IN_TRANSIT 전환 성공")
        void SHIPPED_에서_IN_TRANSIT_전환_성공() {
            // Arrange
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.SHIPPED);
            entityManager.flush();

            // Act
            courierAdminOrderAppService.updateStatus(order.getId(), CourierOrderStatus.IN_TRANSIT);

            // Assert
            entityManager.flush();
            entityManager.clear();
            CourierOrder updated = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(CourierOrderStatus.IN_TRANSIT);
        }

        @Test
        @DisplayName("SHIPPED → DELIVERED 전환 성공")
        void SHIPPED_에서_DELIVERED_전환_성공() {
            // Arrange
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.SHIPPED);
            entityManager.flush();

            // Act
            courierAdminOrderAppService.updateStatus(order.getId(), CourierOrderStatus.DELIVERED);

            // Assert
            entityManager.flush();
            entityManager.clear();
            CourierOrder updated = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(CourierOrderStatus.DELIVERED);
        }

        @Test
        @DisplayName("IN_TRANSIT → DELIVERED 전환 성공")
        void IN_TRANSIT_에서_DELIVERED_전환_성공() {
            // Arrange
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.IN_TRANSIT);
            entityManager.flush();

            // Act
            courierAdminOrderAppService.updateStatus(order.getId(), CourierOrderStatus.DELIVERED);

            // Assert
            entityManager.flush();
            entityManager.clear();
            CourierOrder updated = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(CourierOrderStatus.DELIVERED);
        }

        @Test
        @DisplayName("PAID → DELIVERED 시도 시 예외 발생")
        void PAID_에서_DELIVERED_시도시_예외() {
            // Arrange
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.PAID);
            entityManager.flush();

            // Act & Assert
            assertThatThrownBy(() ->
                    courierAdminOrderAppService.updateStatus(order.getId(), CourierOrderStatus.DELIVERED))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("배송완료는 발송완료 또는 배송중 상태에서만 가능합니다");
        }

        @Test
        @DisplayName("PAID → IN_TRANSIT 시도 시 예외 발생")
        void PAID_에서_IN_TRANSIT_시도시_예외() {
            // Arrange
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.PAID);
            entityManager.flush();

            // Act & Assert
            assertThatThrownBy(() ->
                    courierAdminOrderAppService.updateStatus(order.getId(), CourierOrderStatus.IN_TRANSIT))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("배송중은 발송완료 상태에서만 가능합니다");
        }

        @Test
        @DisplayName("DELIVERED → PREPARING 시도 시 예외 발생")
        void DELIVERED_에서_PREPARING_시도시_예외() {
            // Arrange
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.DELIVERED);
            entityManager.flush();

            // Act & Assert
            assertThatThrownBy(() ->
                    courierAdminOrderAppService.updateStatus(order.getId(), CourierOrderStatus.PREPARING))
                    .isInstanceOf(AdminValidateException.class);
        }
    }

    @Nested
    @DisplayName("발송 처리 테스트 - ship()")
    class ShipTests {

        @Test
        @DisplayName("PAID 상태에서 ship() 호출 시 SHIPPED 전환 및 운송장 번호 저장")
        void PAID_상태_발송처리_성공() {
            // Arrange
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.PAID);
            entityManager.flush();
            String waybillNumber = "1234567890";

            // Act
            courierAdminOrderAppService.ship(order.getId(), waybillNumber);

            // Assert
            entityManager.flush();
            entityManager.clear();
            CourierOrder updated = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(CourierOrderStatus.SHIPPED);
            assertThat(updated.getWaybillNumber()).isEqualTo(waybillNumber);
        }

        @Test
        @DisplayName("PREPARING 상태에서 ship() 호출 시 SHIPPED 전환 성공")
        void PREPARING_상태_발송처리_성공() {
            // Arrange
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.PREPARING);
            entityManager.flush();
            String waybillNumber = "9876543210";

            // Act
            courierAdminOrderAppService.ship(order.getId(), waybillNumber);

            // Assert
            entityManager.flush();
            entityManager.clear();
            CourierOrder updated = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(CourierOrderStatus.SHIPPED);
            assertThat(updated.getWaybillNumber()).isEqualTo(waybillNumber);
        }

        @Test
        @DisplayName("SHIPPED 상태에서 ship() 시도 시 예외 발생")
        void SHIPPED_상태_발송처리_시도시_예외() {
            // Arrange
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.SHIPPED);
            entityManager.flush();

            // Act & Assert
            assertThatThrownBy(() ->
                    courierAdminOrderAppService.ship(order.getId(), "1111111111"))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("발송 처리는 결제완료 또는 준비중 상태에서만 가능합니다");
        }
    }

    @Nested
    @DisplayName("관리자 전액 포인트 주문 취소 - cancel()")
    class CancelWithFullPoint {

        @Test
        @DisplayName("전액 포인트(13000) PAID 주문 관리자 취소 시 PG 환불 없이 포인트 전액 환원")
        void 전액_포인트_PAID_주문_관리자_취소_포인트_전액환원() {
            // Arrange
            BigDecimal totalAmount = new BigDecimal("13000");
            givePoints(user, totalAmount);
            entityManager.flush();
            entityManager.clear();

            CourierProduct product = saveCourierProduct("전액포인트상품");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.PAID, totalAmount);
            addOrderItem(order, product);
            entityManager.flush();

            BigDecimal balanceBefore = userPointService.getBalance(user.getUid());

            // Act
            // pgPaymentAmount = totalAmount(13000) - pointUsed(13000) = 0 이므로 PG 환불 스킵
            courierAdminOrderAppService.cancel(order.getId());

            // Assert
            entityManager.flush();
            entityManager.clear();

            assertThat(order.getPgPaymentAmount()).isEqualByComparingTo(BigDecimal.ZERO);

            BigDecimal balanceAfter = userPointService.getBalance(user.getUid());
            assertThat(balanceAfter).isEqualByComparingTo(balanceBefore.add(totalAmount));

            CourierOrder canceled = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(canceled.getStatus()).isEqualTo(CourierOrderStatus.CANCELED);
        }
    }
}
