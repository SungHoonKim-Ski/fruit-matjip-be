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
import store.onuljang.courier.appservice.CourierOrderAppService;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierOrderItem;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.repository.CourierOrderRepository;
import store.onuljang.courier.repository.CourierProductRepository;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.exception.AdminValidateException;
import store.onuljang.shared.exception.NotFoundException;
import store.onuljang.shared.exception.UserValidateException;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.user.service.UserPointService;
import store.onuljang.shared.util.DisplayCodeGenerator;
import store.onuljang.support.IntegrationTestBase;

@DisplayName("택배 주문 취소 통합 테스트")
class CourierOrderCancelIntegrationTest extends IntegrationTestBase {

    @Autowired
    private CourierOrderAppService courierOrderAppService;

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
        user = testFixture.createUser("취소테스트");
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

    private CourierOrder saveCourierOrderFullPoint(Users user, CourierOrderStatus status) {
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
                .pointUsed(totalAmount)
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

    /**
     * cancel()/fail() 처럼 내부에서 resolveCode()를 호출하는 AppService 메서드를 테스트할 때 사용.
     * DisplayCodeGenerator.generate()로 유효한 형식의 displayCode를 생성한다.
     */
    private CourierOrder saveCourierOrderWithValidCode(Users user, CourierOrderStatus status) {
        return saveCourierOrderWithValidCode(user, status, BigDecimal.ZERO);
    }

    private CourierOrder saveCourierOrderWithValidCode(Users user, CourierOrderStatus status, BigDecimal pointUsed) {
        String displayCode = DisplayCodeGenerator.generate("C", java.time.LocalDateTime.now());
        BigDecimal totalAmount = new BigDecimal("13000");
        CourierOrder order = CourierOrder.builder()
                .user(user)
                .displayCode(displayCode)
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

    private void givePoints(Users user, BigDecimal amount) {
        userPointService.earn(
                user.getUid(), amount,
                store.onuljang.shared.entity.enums.UserPointTransactionType.EARN_ADMIN,
                "테스트 포인트 지급", "TEST", null, "test");
    }

    @Nested
    @DisplayName("사용자 결제 후 취소 - cancelPaidOrder()")
    class UserCancelPaidOrder {

        @Test
        @DisplayName("PAID 상태 주문을 사용자가 취소하면 CANCELED로 전환된다")
        void PAID_상태_사용자_취소_성공() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품A");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.PAID);
            addOrderItem(order, product);
            entityManager.flush();

            // Act
            courierOrderAppService.cancelPaidOrder(user.getUid(), order.getDisplayCode());

            // Assert
            entityManager.flush();
            entityManager.clear();
            CourierOrder canceled = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(canceled.getStatus()).isEqualTo(CourierOrderStatus.CANCELED);
        }

        @Test
        @DisplayName("ORDERING 상태 주문을 사용자가 취소하면 CANCELED로 전환된다")
        void ORDERING_상태_사용자_취소_성공() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품B");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.ORDERING);
            addOrderItem(order, product);
            entityManager.flush();

            // Act
            courierOrderAppService.cancelPaidOrder(user.getUid(), order.getDisplayCode());

            // Assert
            entityManager.flush();
            entityManager.clear();
            CourierOrder canceled = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(canceled.getStatus()).isEqualTo(CourierOrderStatus.CANCELED);
        }

        @Test
        @DisplayName("ORDER_COMPLETED 상태 주문을 사용자가 취소하면 400 예외가 발생한다")
        void ORDER_COMPLETED_상태_사용자_취소_실패() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품C");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.ORDER_COMPLETED);
            addOrderItem(order, product);
            entityManager.flush();

            // Act & Assert
            assertThatThrownBy(() ->
                    courierOrderAppService.cancelPaidOrder(user.getUid(), order.getDisplayCode()))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("발주완료 이후 주문은 취소할 수 없습니다.");
        }

        @Test
        @DisplayName("포인트 사용 주문 취소 시 PG 환불금액은 pgPaymentAmount이고 포인트가 환원된다")
        void 포인트_사용_주문_취소_포인트_환원() {
            // Arrange
            BigDecimal pointUsed = new BigDecimal("3000");
            // pgPaymentAmount = totalAmount(13000) - pointUsed(3000) = 10000
            givePoints(user, pointUsed);
            entityManager.flush();
            entityManager.clear();

            CourierProduct product = saveCourierProduct("상품D");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.PAID, pointUsed);
            addOrderItem(order, product);
            entityManager.flush();

            BigDecimal balanceBefore = userPointService.getBalance(user.getUid());

            // Act
            courierOrderAppService.cancelPaidOrder(user.getUid(), order.getDisplayCode());

            // Assert
            entityManager.flush();
            entityManager.clear();

            // pgPaymentAmount 검증: totalAmount(13000) - pointUsed(3000) = 10000
            assertThat(order.getPgPaymentAmount()).isEqualByComparingTo(new BigDecimal("10000"));

            // 포인트 환원 검증
            BigDecimal balanceAfter = userPointService.getBalance(user.getUid());
            assertThat(balanceAfter).isEqualByComparingTo(balanceBefore.add(pointUsed));

            CourierOrder canceled = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(canceled.getStatus()).isEqualTo(CourierOrderStatus.CANCELED);
        }

        @Test
        @DisplayName("전액 포인트 주문 취소 시 PG 환불 없이 포인트 전액 환원된다")
        void 전액_포인트_주문_취소_PG환불없음_포인트전액환원() {
            // Arrange
            BigDecimal totalAmount = new BigDecimal("13000");
            givePoints(user, totalAmount);
            entityManager.flush();
            entityManager.clear();

            CourierProduct product = saveCourierProduct("상품E");
            CourierOrder order = saveCourierOrderFullPoint(user, CourierOrderStatus.PAID);
            addOrderItem(order, product);
            entityManager.flush();

            BigDecimal balanceBefore = userPointService.getBalance(user.getUid());

            // Act
            // pgPaymentAmount = 0 이므로 PG 환불 스킵, 포인트 전액 환원
            courierOrderAppService.cancelPaidOrder(user.getUid(), order.getDisplayCode());

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

    @Nested
    @DisplayName("관리자 취소 버그픽스 - cancel() ORDERING + 포인트 환원")
    class AdminCancelBugfix {

        @Test
        @DisplayName("ORDERING 상태 주문을 관리자가 취소하면 CANCELED로 전환된다 (버그픽스)")
        void ORDERING_상태_관리자_취소_성공() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품F");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.ORDERING);
            addOrderItem(order, product);
            entityManager.flush();

            // Act
            courierAdminOrderAppService.cancel(order.getId());

            // Assert
            entityManager.flush();
            entityManager.clear();
            CourierOrder canceled = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(canceled.getStatus()).isEqualTo(CourierOrderStatus.CANCELED);
        }

        @Test
        @DisplayName("ORDERING 포인트 사용 주문 관리자 취소 시 포인트가 환원된다 (버그픽스)")
        void ORDERING_포인트_주문_관리자_취소_포인트_환원() {
            // Arrange
            BigDecimal pointUsed = new BigDecimal("5000");
            givePoints(user, pointUsed);
            entityManager.flush();
            entityManager.clear();

            CourierProduct product = saveCourierProduct("상품G");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.ORDERING, pointUsed);
            addOrderItem(order, product);
            entityManager.flush();

            BigDecimal balanceBefore = userPointService.getBalance(user.getUid());

            // Act
            courierAdminOrderAppService.cancel(order.getId());

            // Assert
            entityManager.flush();
            entityManager.clear();

            BigDecimal balanceAfter = userPointService.getBalance(user.getUid());
            assertThat(balanceAfter).isEqualByComparingTo(balanceBefore.add(pointUsed));

            CourierOrder canceled = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(canceled.getStatus()).isEqualTo(CourierOrderStatus.CANCELED);
        }
    }

    @Nested
    @DisplayName("사용자 취소 엣지케이스")
    class UserCancelEdgeCases {

        @Test
        @DisplayName("다른 사용자의 주문을 취소하면 NotFoundException이 발생한다")
        void 다른_사용자_주문_취소_시도_예외() {
            // Arrange
            Users user2 = testFixture.createUser("다른사용자");
            CourierProduct product = saveCourierProduct("상품H");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.PAID);
            addOrderItem(order, product);
            entityManager.flush();

            // Act & Assert
            assertThatThrownBy(() ->
                    courierOrderAppService.cancelPaidOrder(user2.getUid(), order.getDisplayCode()))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("DELIVERED 상태 주문을 사용자가 취소하면 예외가 발생한다")
        void DELIVERED_상태_사용자_취소_실패() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품I");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.DELIVERED);
            addOrderItem(order, product);
            entityManager.flush();

            // Act & Assert
            assertThatThrownBy(() ->
                    courierOrderAppService.cancelPaidOrder(user.getUid(), order.getDisplayCode()))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("발주완료 이후 주문은 취소할 수 없습니다.");
        }

        @Test
        @DisplayName("IN_TRANSIT 상태 주문을 사용자가 취소하면 예외가 발생한다")
        void IN_TRANSIT_상태_사용자_취소_실패() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품J");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.IN_TRANSIT);
            addOrderItem(order, product);
            entityManager.flush();

            // Act & Assert
            assertThatThrownBy(() ->
                    courierOrderAppService.cancelPaidOrder(user.getUid(), order.getDisplayCode()))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("발주완료 이후 주문은 취소할 수 없습니다.");
        }

        @Test
        @DisplayName("이미 CANCELED 상태 주문을 재취소하면 예외가 발생한다")
        void CANCELED_상태_사용자_재취소_실패() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품K");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.CANCELED);
            addOrderItem(order, product);
            entityManager.flush();

            // Act & Assert
            assertThatThrownBy(() ->
                    courierOrderAppService.cancelPaidOrder(user.getUid(), order.getDisplayCode()))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("발주완료 이후 주문은 취소할 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("재고 복원 검증")
    class StockRestoreTests {

        @Test
        @DisplayName("PAID 상태 주문 취소 시 restoreStock이 정상 호출되어 예외 없이 완료된다")
        void PAID_주문_취소_재고복원_정상완료() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품L");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.PAID);
            addOrderItem(order, product);
            entityManager.flush();

            // Act
            courierOrderAppService.cancelPaidOrder(user.getUid(), order.getDisplayCode());

            // Assert
            entityManager.flush();
            entityManager.clear();
            CourierOrder canceled = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(canceled.getStatus()).isEqualTo(CourierOrderStatus.CANCELED);
        }

        @Test
        @DisplayName("ORDERING 상태 주문 관리자 취소 시 restoreStock이 정상 호출되어 예외 없이 완료된다")
        void ORDERING_주문_관리자_취소_재고복원_정상완료() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품M");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.ORDERING);
            addOrderItem(order, product);
            entityManager.flush();

            // Act
            courierAdminOrderAppService.cancel(order.getId());

            // Assert
            entityManager.flush();
            entityManager.clear();
            CourierOrder canceled = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(canceled.getStatus()).isEqualTo(CourierOrderStatus.CANCELED);
        }
    }

    @Nested
    @DisplayName("관리자 취소 엣지케이스")
    class AdminCancelEdgeCases {

        @Test
        @DisplayName("ORDER_COMPLETED 상태 주문을 관리자가 취소하면 AdminValidateException이 발생한다")
        void ORDER_COMPLETED_상태_관리자_취소_실패() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품N");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.ORDER_COMPLETED);
            addOrderItem(order, product);
            entityManager.flush();

            // Act & Assert
            assertThatThrownBy(() ->
                    courierAdminOrderAppService.cancel(order.getId()))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("발주완료 이후 또는 취소된 주문은 취소할 수 없습니다.");
        }

        @Test
        @DisplayName("DELIVERED 상태 주문을 관리자가 취소하면 AdminValidateException이 발생한다")
        void DELIVERED_상태_관리자_취소_실패() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품O");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.DELIVERED);
            addOrderItem(order, product);
            entityManager.flush();

            // Act & Assert
            assertThatThrownBy(() ->
                    courierAdminOrderAppService.cancel(order.getId()))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("발주완료 이후 또는 취소된 주문은 취소할 수 없습니다.");
        }

        @Test
        @DisplayName("CANCELED 상태 주문을 관리자가 재취소하면 AdminValidateException이 발생한다")
        void CANCELED_상태_관리자_재취소_실패() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품P");
            CourierOrder order = saveCourierOrder(user, CourierOrderStatus.CANCELED);
            addOrderItem(order, product);
            entityManager.flush();

            // Act & Assert
            assertThatThrownBy(() ->
                    courierAdminOrderAppService.cancel(order.getId()))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("발주완료 이후 또는 취소된 주문은 취소할 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("결제 전 취소 - cancel()")
    class UserCancelPendingPayment {

        @Test
        @DisplayName("PENDING_PAYMENT 상태 주문을 cancel() 하면 CANCELED로 전환된다")
        void PENDING_PAYMENT_취소_성공() {
            // Arrange
            // cancel()은 내부에서 resolveCode()로 형식 검증하므로 유효한 displayCode 필요
            CourierProduct product = saveCourierProduct("상품Q");
            CourierOrder order = saveCourierOrderWithValidCode(user, CourierOrderStatus.PENDING_PAYMENT);
            addOrderItem(order, product);
            entityManager.flush();

            // Act
            courierOrderAppService.cancel(user.getUid(), order.getDisplayCode());

            // Assert
            entityManager.flush();
            entityManager.clear();
            CourierOrder canceled = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(canceled.getStatus()).isEqualTo(CourierOrderStatus.CANCELED);
        }

        @Test
        @DisplayName("PENDING_PAYMENT 포인트 사용 주문 cancel() 시 포인트가 환원된다")
        void PENDING_PAYMENT_포인트_주문_취소_포인트_환원() {
            // Arrange
            BigDecimal pointUsed = new BigDecimal("4000");
            givePoints(user, pointUsed);
            entityManager.flush();
            entityManager.clear();

            CourierProduct product = saveCourierProduct("상품R");
            CourierOrder order = saveCourierOrderWithValidCode(user, CourierOrderStatus.PENDING_PAYMENT, pointUsed);
            addOrderItem(order, product);
            entityManager.flush();

            BigDecimal balanceBefore = userPointService.getBalance(user.getUid());

            // Act
            courierOrderAppService.cancel(user.getUid(), order.getDisplayCode());

            // Assert
            entityManager.flush();
            entityManager.clear();

            BigDecimal balanceAfter = userPointService.getBalance(user.getUid());
            assertThat(balanceAfter).isEqualByComparingTo(balanceBefore.add(pointUsed));

            CourierOrder canceled = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(canceled.getStatus()).isEqualTo(CourierOrderStatus.CANCELED);
        }
    }

    @Nested
    @DisplayName("결제 실패 시 포인트 환원 - fail()")
    class FailWithPointRefund {

        @Test
        @DisplayName("PENDING_PAYMENT 포인트 사용 주문 fail() 시 포인트가 환원된다")
        void PENDING_PAYMENT_포인트_주문_fail_포인트_환원() {
            // Arrange
            // fail()은 내부에서 resolveCode()로 형식 검증하므로 유효한 displayCode 필요
            BigDecimal pointUsed = new BigDecimal("6000");
            givePoints(user, pointUsed);
            entityManager.flush();
            entityManager.clear();

            CourierProduct product = saveCourierProduct("상품S");
            CourierOrder order = saveCourierOrderWithValidCode(user, CourierOrderStatus.PENDING_PAYMENT, pointUsed);
            addOrderItem(order, product);
            entityManager.flush();

            BigDecimal balanceBefore = userPointService.getBalance(user.getUid());

            // Act
            courierOrderAppService.fail(user.getUid(), order.getDisplayCode());

            // Assert
            entityManager.flush();
            entityManager.clear();

            BigDecimal balanceAfter = userPointService.getBalance(user.getUid());
            assertThat(balanceAfter).isEqualByComparingTo(balanceBefore.add(pointUsed));

            CourierOrder failed = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(failed.getStatus()).isEqualTo(CourierOrderStatus.FAILED);
        }

        @Test
        @DisplayName("PENDING_PAYMENT 포인트 미사용 주문 fail() 시 상태가 FAILED로 전환된다")
        void PENDING_PAYMENT_포인트_미사용_주문_fail_상태전환() {
            // Arrange
            CourierProduct product = saveCourierProduct("상품T");
            CourierOrder order = saveCourierOrderWithValidCode(user, CourierOrderStatus.PENDING_PAYMENT);
            addOrderItem(order, product);
            entityManager.flush();

            // Act
            courierOrderAppService.fail(user.getUid(), order.getDisplayCode());

            // Assert
            entityManager.flush();
            entityManager.clear();
            CourierOrder failed = courierOrderRepository.findById(order.getId()).orElseThrow();
            assertThat(failed.getStatus()).isEqualTo(CourierOrderStatus.FAILED);
        }
    }
}
