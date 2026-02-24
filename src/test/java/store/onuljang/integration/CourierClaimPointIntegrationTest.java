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
import store.onuljang.courier.dto.CourierClaimApproveRequest;
import store.onuljang.courier.dto.CourierClaimRequest;
import store.onuljang.courier.dto.CourierClaimResponse;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierOrderItem;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.repository.CourierOrderRepository;
import store.onuljang.courier.repository.CourierProductRepository;
import store.onuljang.shared.entity.enums.CourierClaimReturnStatus;
import store.onuljang.shared.entity.enums.CourierClaimType;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.exception.AdminValidateException;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.user.service.UserPointService;
import store.onuljang.support.IntegrationTestBase;

@DisplayName("Courier 클레임 포인트 통합 테스트")
class CourierClaimPointIntegrationTest extends IntegrationTestBase {

    @Autowired
    private CourierClaimAppService courierClaimAppService;

    @Autowired
    private UserPointService userPointService;

    @Autowired
    private CourierProductRepository courierProductRepository;

    @Autowired
    private CourierOrderRepository courierOrderRepository;

    @Autowired
    private EntityManager entityManager;

    private Users user;

    @BeforeEach
    void setUp() {
        user = testFixture.createUser("클레임테스트");
    }

    // --- helper methods (mirrored from CourierClaimIntegrationTest) ---

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

    // --- 클레임 접수 헬퍼 ---

    private CourierClaimResponse createQualityIssueClaim(CourierOrder order) {
        CourierClaimRequest claimReq = new CourierClaimRequest(
                CourierClaimType.QUALITY_ISSUE, null, "품질 문제로 클레임 접수");
        return courierClaimAppService.createClaim(user.getUid(), order.getDisplayCode(), claimReq);
    }

    @Nested
    @DisplayName("클레임 승인 시 포인트 발행")
    class ApproveWithPointTests {

        @Test
        @DisplayName("클레임 승인 시 pointAmount만큼 포인트가 발행된다")
        void 클레임_승인_포인트_발행() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품A");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.DELIVERED);
            addOrderItem(order, product);
            entityManager.flush();

            CourierClaimResponse claim = createQualityIssueClaim(order);

            // Act
            CourierClaimApproveRequest approveReq = new CourierClaimApproveRequest(
                    "NOTE", "포인트 지급", null, new BigDecimal("2000"), false);
            CourierClaimResponse result = courierClaimAppService.approveClaim(claim.id(), approveReq);

            // Assert
            assertThat(result.pointAmount()).isEqualByComparingTo(new BigDecimal("2000"));

            entityManager.flush();
            entityManager.clear();
            assertThat(userPointService.getBalance(user.getUid()))
                    .isEqualByComparingTo(new BigDecimal("2000"));
        }

        @Test
        @DisplayName("클레임 승인 시 REFUND action과 pointAmount를 함께 지정하면 환불과 포인트가 모두 처리된다")
        void 클레임_승인_환불_및_포인트() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품B");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.DELIVERED);
            addOrderItem(order, product);
            entityManager.flush();

            CourierClaimResponse claim = createQualityIssueClaim(order);

            // Act
            CourierClaimApproveRequest approveReq = new CourierClaimApproveRequest(
                    "REFUND", "환불+포인트", null, new BigDecimal("2000"), false);
            CourierClaimResponse result = courierClaimAppService.approveClaim(claim.id(), approveReq);

            // Assert
            assertThat(result.pointAmount()).isEqualByComparingTo(new BigDecimal("2000"));

            entityManager.flush();
            entityManager.clear();
            assertThat(userPointService.getBalance(user.getUid()))
                    .isEqualByComparingTo(new BigDecimal("2000"));
        }

        @Test
        @DisplayName("pointAmount가 null이면 포인트가 발행되지 않는다")
        void 클레임_승인_포인트_없음() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품C");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.DELIVERED);
            addOrderItem(order, product);
            entityManager.flush();

            CourierClaimResponse claim = createQualityIssueClaim(order);

            // Act
            CourierClaimApproveRequest approveReq = new CourierClaimApproveRequest(
                    "NOTE", "포인트 없음", null, null, false);
            CourierClaimResponse result = courierClaimAppService.approveClaim(claim.id(), approveReq);

            // Assert
            assertThat(result.pointAmount()).isNull();

            entityManager.flush();
            entityManager.clear();
            assertThat(userPointService.getBalance(user.getUid()))
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("pointAmount가 0이면 포인트가 발행되지 않는다")
        void 클레임_승인_포인트_0원() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품D");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.DELIVERED);
            addOrderItem(order, product);
            entityManager.flush();

            CourierClaimResponse claim = createQualityIssueClaim(order);

            // Act
            CourierClaimApproveRequest approveReq = new CourierClaimApproveRequest(
                    "NOTE", "0원 포인트", null, BigDecimal.ZERO, false);
            courierClaimAppService.approveClaim(claim.id(), approveReq);

            // Assert
            entityManager.flush();
            entityManager.clear();
            assertThat(userPointService.getBalance(user.getUid()))
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("클레임 승인 시 회수 상태 설정")
    class ApproveWithReturnStatusTests {

        @Test
        @DisplayName("returnRequired=true이면 회수 상태가 COLLECTING으로 설정된다")
        void 클레임_승인_회수_상태_COLLECTING() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품E");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.DELIVERED);
            addOrderItem(order, product);
            entityManager.flush();

            CourierClaimResponse claim = createQualityIssueClaim(order);

            // Act
            CourierClaimApproveRequest approveReq = new CourierClaimApproveRequest(
                    "NOTE", "회수 요청", null, null, true);
            CourierClaimResponse result = courierClaimAppService.approveClaim(claim.id(), approveReq);

            // Assert
            assertThat(result.returnStatus()).isEqualTo(CourierClaimReturnStatus.COLLECTING);
        }

        @Test
        @DisplayName("returnRequired=false이면 회수 상태가 설정되지 않는다")
        void 클레임_승인_회수_없음() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품F");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.DELIVERED);
            addOrderItem(order, product);
            entityManager.flush();

            CourierClaimResponse claim = createQualityIssueClaim(order);

            // Act
            CourierClaimApproveRequest approveReq = new CourierClaimApproveRequest(
                    "NOTE", "회수 없음", null, null, false);
            CourierClaimResponse result = courierClaimAppService.approveClaim(claim.id(), approveReq);

            // Assert
            assertThat(result.returnStatus()).isEqualTo(CourierClaimReturnStatus.NONE);
        }

        @Test
        @DisplayName("포인트와 회수 상태를 동시에 설정할 수 있다")
        void 포인트_및_회수_상태_동시_설정() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품G");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.DELIVERED);
            addOrderItem(order, product);
            entityManager.flush();

            CourierClaimResponse claim = createQualityIssueClaim(order);

            // Act
            CourierClaimApproveRequest approveReq = new CourierClaimApproveRequest(
                    "NOTE", "포인트+회수", null, new BigDecimal("3000"), true);
            CourierClaimResponse result = courierClaimAppService.approveClaim(claim.id(), approveReq);

            // Assert
            assertThat(result.pointAmount()).isEqualByComparingTo(new BigDecimal("3000"));
            assertThat(result.returnStatus()).isEqualTo(CourierClaimReturnStatus.COLLECTING);

            entityManager.flush();
            entityManager.clear();
            assertThat(userPointService.getBalance(user.getUid()))
                    .isEqualByComparingTo(new BigDecimal("3000"));
        }
    }

    @Nested
    @DisplayName("클레임 회수 상태 변경")
    class ReturnStatusUpdateTests {

        @Test
        @DisplayName("회수 상태를 COLLECTED로 변경할 수 있다")
        void 회수_상태_COLLECTED_변경() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품H");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.DELIVERED);
            addOrderItem(order, product);
            entityManager.flush();

            CourierClaimResponse claim = createQualityIssueClaim(order);
            CourierClaimApproveRequest approveReq = new CourierClaimApproveRequest(
                    "NOTE", "회수 시작", null, null, true);
            courierClaimAppService.approveClaim(claim.id(), approveReq);

            // Act
            CourierClaimResponse result = courierClaimAppService.updateReturnStatus(
                    claim.id(), "COLLECTED");

            // Assert
            assertThat(result.returnStatus()).isEqualTo(CourierClaimReturnStatus.COLLECTED);
        }

        @Test
        @DisplayName("유효하지 않은 회수 상태 값이면 예외가 발생한다")
        void 유효하지_않은_회수_상태_예외() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품I");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.DELIVERED);
            addOrderItem(order, product);
            entityManager.flush();

            CourierClaimResponse claim = createQualityIssueClaim(order);

            // Act & Assert
            assertThatThrownBy(() -> courierClaimAppService.updateReturnStatus(
                    claim.id(), "INVALID_STATUS"))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("유효하지 않은 회수 상태입니다");
        }
    }

    @Nested
    @DisplayName("이미 처리된 클레임 승인 불가")
    class AlreadyResolvedTests {

        @Test
        @DisplayName("RESOLVED 상태 클레임은 재승인 시 예외가 발생한다")
        void 이미_해결된_클레임_재승인_예외() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품J");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.DELIVERED);
            addOrderItem(order, product);
            entityManager.flush();

            CourierClaimResponse claim = createQualityIssueClaim(order);
            // REFUND action → claim이 RESOLVED 상태로 변경됨
            CourierClaimApproveRequest firstApprove = new CourierClaimApproveRequest(
                    "REFUND", "첫 번째 승인", null, null, false);
            courierClaimAppService.approveClaim(claim.id(), firstApprove);

            // Act & Assert
            CourierClaimApproveRequest secondApprove = new CourierClaimApproveRequest(
                    "REFUND", "두 번째 승인 시도", null, null, false);
            assertThatThrownBy(() -> courierClaimAppService.approveClaim(claim.id(), secondApprove))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("승인 가능한 상태가 아닙니다");
        }

        @Test
        @DisplayName("REJECTED 상태 클레임은 승인 시 예외가 발생한다")
        void 거부된_클레임_승인_예외() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품K");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.DELIVERED);
            addOrderItem(order, product);
            entityManager.flush();

            CourierClaimResponse claim = createQualityIssueClaim(order);
            courierClaimAppService.rejectClaim(claim.id(), "거부 사유");

            // Act & Assert
            CourierClaimApproveRequest approveReq = new CourierClaimApproveRequest(
                    "NOTE", "거부 후 승인 시도", null, null, false);
            assertThatThrownBy(() -> courierClaimAppService.approveClaim(claim.id(), approveReq))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("승인 가능한 상태가 아닙니다");
        }
    }
}
