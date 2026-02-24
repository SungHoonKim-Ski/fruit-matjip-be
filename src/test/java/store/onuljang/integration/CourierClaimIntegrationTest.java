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
import store.onuljang.courier.appservice.CourierClaimAppService;
import store.onuljang.courier.dto.CourierClaimRequest;
import store.onuljang.courier.dto.CourierClaimResponse;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierOrderItem;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.repository.CourierOrderRepository;
import store.onuljang.courier.repository.CourierProductRepository;
import store.onuljang.shared.entity.enums.CourierClaimType;
import store.onuljang.shared.entity.enums.CourierOrderItemStatus;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.exception.UserValidateException;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.support.IntegrationTestBase;

@DisplayName("Courier 클레임 통합 테스트")
class CourierClaimIntegrationTest extends IntegrationTestBase {

    @Autowired
    private CourierClaimAppService courierClaimAppService;

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
                .totalAmount(new BigDecimal("13000"))
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

    @Nested
    @DisplayName("클레임 상태 검증")
    class ClaimStatusValidation {

        @Test
        @DisplayName("PAID 상태에서 클레임 접수 가능")
        void PAID_상태_클레임_가능() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품A");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.PAID);
            addOrderItem(order, product);
            entityManager.flush();

            CourierClaimRequest request = new CourierClaimRequest(
                    CourierClaimType.QUALITY_ISSUE, null, "상품에 문제가 있습니다. 교환 요청합니다.");

            // Act
            CourierClaimResponse response = courierClaimAppService.createClaim(
                    user.getUid(), order.getDisplayCode(), request);

            // Assert
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("DELIVERED 상태에서 클레임 접수 가능")
        void DELIVERED_상태_클레임_가능() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품B");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.DELIVERED);
            addOrderItem(order, product);
            entityManager.flush();

            CourierClaimRequest request = new CourierClaimRequest(
                    CourierClaimType.CHANGE_OF_MIND, null, "단순 변심으로 반품 요청합니다.");

            // Act
            CourierClaimResponse response = courierClaimAppService.createClaim(
                    user.getUid(), order.getDisplayCode(), request);

            // Assert
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("ORDER_COMPLETED 상태에서 클레임 접수 가능")
        void ORDER_COMPLETED_상태_클레임_가능() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품C");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.ORDER_COMPLETED);
            addOrderItem(order, product);
            entityManager.flush();

            CourierClaimRequest request = new CourierClaimRequest(
                    CourierClaimType.QUALITY_ISSUE, null, "배송 중이지만 문의사항이 있습니다.");

            // Act
            CourierClaimResponse response = courierClaimAppService.createClaim(
                    user.getUid(), order.getDisplayCode(), request);

            // Assert
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("PENDING_PAYMENT 상태에서 클레임 접수 불가")
        void PENDING_PAYMENT_상태_클레임_불가() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품D");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.PENDING_PAYMENT);
            addOrderItem(order, product);
            entityManager.flush();

            CourierClaimRequest request = new CourierClaimRequest(
                    CourierClaimType.QUALITY_ISSUE, null, "결제 전 문의입니다.");

            // Act & Assert
            assertThatThrownBy(() -> courierClaimAppService.createClaim(
                    user.getUid(), order.getDisplayCode(), request))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("결제 완료 후 문의가 가능합니다.");
        }

        @Test
        @DisplayName("CANCELED 상태에서 클레임 접수 불가")
        void CANCELED_상태_클레임_불가() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품E");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.CANCELED);
            addOrderItem(order, product);
            entityManager.flush();

            CourierClaimRequest request = new CourierClaimRequest(
                    CourierClaimType.QUALITY_ISSUE, null, "취소된 주문입니다.");

            // Act & Assert
            assertThatThrownBy(() -> courierClaimAppService.createClaim(
                    user.getUid(), order.getDisplayCode(), request))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("결제 완료 후 문의가 가능합니다.");
        }

        @Test
        @DisplayName("FAILED 상태에서 클레임 접수 불가")
        void FAILED_상태_클레임_불가() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품F");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.FAILED);
            addOrderItem(order, product);
            entityManager.flush();

            CourierClaimRequest request = new CourierClaimRequest(
                    CourierClaimType.QUALITY_ISSUE, null, "실패된 주문입니다.");

            // Act & Assert
            assertThatThrownBy(() -> courierClaimAppService.createClaim(
                    user.getUid(), order.getDisplayCode(), request))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("결제 완료 후 문의가 가능합니다.");
        }
    }

    @Nested
    @DisplayName("클레임 상품 선택")
    class ClaimItemSelection {

        @Test
        @DisplayName("courierOrderItemId(상품 ID)로 특정 상품 선택 시 해당 항목이 CLAIM_REQUESTED로 변경된다")
        void 상품ID로_항목_선택() {
            // Arrange
            CourierProduct productA = saveCourierProduct("상품A");
            CourierProduct productB = saveCourierProduct("상품B");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.DELIVERED);
            addOrderItem(order, productA);
            addOrderItem(order, productB);
            entityManager.flush();

            // courierOrderItemId 필드는 실제로 상품 ID를 받아 필터링한다
            CourierClaimRequest request = new CourierClaimRequest(
                    CourierClaimType.QUALITY_ISSUE, productA.getId(), "상품A에 문제가 있습니다.");

            // Act
            CourierClaimResponse response = courierClaimAppService.createClaim(
                    user.getUid(), order.getDisplayCode(), request);

            // Assert
            assertThat(response).isNotNull();
            entityManager.flush();
            entityManager.clear();
            CourierOrder reloaded = courierOrderRepository.findByIdWithItems(order.getId()).orElseThrow();
            CourierOrderItem claimedItem = reloaded.getItems().stream()
                    .filter(i -> i.getCourierProduct().getId().equals(productA.getId()))
                    .findFirst()
                    .orElseThrow();
            assertThat(claimedItem.getItemStatus()).isEqualTo(CourierOrderItemStatus.CLAIM_REQUESTED);
        }

        @Test
        @DisplayName("courierOrderItemId가 null이면 전체 주문 대상 클레임")
        void 항목_미선택_전체_클레임() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품G");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.DELIVERED);
            addOrderItem(order, product);
            entityManager.flush();

            CourierClaimRequest request = new CourierClaimRequest(
                    CourierClaimType.CHANGE_OF_MIND, null, "전체 주문에 대한 변심 요청입니다.");

            // Act
            CourierClaimResponse response = courierClaimAppService.createClaim(
                    user.getUid(), order.getDisplayCode(), request);

            // Assert
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 클레임 시 예외 발생")
        void 존재하지_않는_상품ID_예외() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품H");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.DELIVERED);
            addOrderItem(order, product);
            entityManager.flush();

            Long nonExistentProductId = -999L;
            CourierClaimRequest request = new CourierClaimRequest(
                    CourierClaimType.QUALITY_ISSUE, nonExistentProductId, "존재하지 않는 상품입니다.");

            // Act & Assert
            assertThatThrownBy(() -> courierClaimAppService.createClaim(
                    user.getUid(), order.getDisplayCode(), request))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("주문에 해당 상품이 없습니다.");
        }
    }
}
