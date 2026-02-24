package store.onuljang.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import store.onuljang.courier.appservice.CourierOrderAppService;
import store.onuljang.courier.dto.*;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierOrderItem;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.service.*;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.entity.enums.PaymentProvider;
import store.onuljang.shared.exception.NotFoundException;
import store.onuljang.shared.exception.UserValidateException;
import store.onuljang.shared.service.KakaoPayService;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.user.service.UserCourierInfoService;
import store.onuljang.shared.user.service.UserService;
import store.onuljang.shop.admin.entity.Admin;

@ExtendWith(MockitoExtension.class)
class CourierOrderAppServiceTest {

    @InjectMocks private CourierOrderAppService courierOrderAppService;

    @Mock private UserService userService;
    @Mock private CourierOrderService courierOrderService;
    @Mock private CourierPaymentService courierPaymentService;
    @Mock private CourierPaymentProcessor courierPaymentProcessor;
    @Mock private CourierProductService courierProductService;
    @Mock private CourierShippingFeeService courierShippingFeeService;
    @Mock private KakaoPayService kakaoPayService;
    @Mock private UserCourierInfoService userCourierInfoService;

    private Users testUser;
    private final String testUid = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        testUser =
                Users.builder()
                        .socialId("social123")
                        .name("테스트유저")
                        .uid(UUID.fromString(testUid))
                        .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);
    }

    private CourierProduct createProduct(String name, BigDecimal price) {
        Admin admin =
                Admin.builder()
                        .name("관리자")
                        .email("admin@test.com")
                        .password("password")
                        .build();
        CourierProduct product =
                CourierProduct.builder()
                        .name(name)
                        .productUrl("https://example.com/img.jpg")
                        .price(price)
                        .visible(true)
                        .registeredAdmin(admin)
                        .build();
        return product;
    }

    private CourierOrder createOrder(
            CourierOrderStatus status, String displayCode, Users user) {
        CourierOrder order =
                CourierOrder.builder()
                        .user(user)
                        .displayCode(displayCode)
                        .status(status)
                        .receiverName("홍길동")
                        .receiverPhone("010-1234-5678")
                        .postalCode("06134")
                        .address1("서울시 강남구")
                        .productAmount(BigDecimal.valueOf(30000))
                        .shippingFee(BigDecimal.valueOf(4000))
                        .totalAmount(BigDecimal.valueOf(34000))
                        .build();
        ReflectionTestUtils.setField(order, "id", 1L);
        return order;
    }

    private CourierOrderReadyRequest createReadyRequest(
            Long productId, int quantity, String idempotencyKey) {
        return new CourierOrderReadyRequest(
                List.of(new CourierOrderReadyRequest.OrderItemRequest(productId, quantity, null)),
                "홍길동",
                "010-1234-5678",
                "06134",
                "서울시 강남구",
                "101호",
                "문 앞에 놓아주세요",
                "KAKAOPAY",
                idempotencyKey,
                null);
    }

    // --- ready ---

    @Nested
    @DisplayName("ready - 주문 준비")
    class Ready {

        @Test
        @DisplayName("정상 주문 생성 - 재고 충분, 배송비 계산, 주문 생성, PG 준비")
        void ready_success() {
            // arrange
            String idempotencyKey = "idem-key-001";
            CourierOrderReadyRequest request = createReadyRequest(1L, 2, idempotencyKey);

            CourierProduct product = createProduct("제주 감귤 5kg", new BigDecimal("15000"));
            ReflectionTestUtils.setField(product, "id", 1L);

            ShippingFeeResult feeResult =
                    new ShippingFeeResult(
                            new BigDecimal("4000"), BigDecimal.ZERO, false, new BigDecimal("4000"));
            CourierOrderReadyResponse expectedResponse =
                    CourierOrderReadyResponse.builder()
                            .displayCode("C-26021400-ABCD2")
                            .redirectUrl("https://kakao.pay/redirect")
                            .build();

            given(userService.findByUId(testUid)).willReturn(testUser);
            given(courierOrderService.findByIdempotencyKey(testUser, idempotencyKey))
                    .willReturn(Optional.empty());
            given(courierOrderService.findPendingPaymentsByUser(testUser))
                    .willReturn(List.of());
            given(courierProductService.findByIdWithLock(1L)).willReturn(product);
            given(courierShippingFeeService.calculateByItems(anyList(), eq("06134"))).willReturn(feeResult);
            given(courierOrderService.existsByDisplayCode(anyString())).willReturn(false);
            given(courierOrderService.save(any(CourierOrder.class)))
                    .willAnswer(
                            invocation -> {
                                CourierOrder saved = invocation.getArgument(0);
                                ReflectionTestUtils.setField(saved, "id", 1L);
                                return saved;
                            });
            given(courierPaymentProcessor.preparePayment(
                            any(CourierOrder.class), eq(testUser), eq(PaymentProvider.KAKAOPAY)))
                    .willReturn(expectedResponse);

            // act
            CourierOrderReadyResponse result = courierOrderAppService.ready(testUid, request);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.displayCode()).isEqualTo("C-26021400-ABCD2");
            verify(courierProductService).findByIdWithLock(1L);
            verify(courierShippingFeeService).calculateByItems(anyList(), eq("06134"));
            verify(courierOrderService).save(any(CourierOrder.class));
            verify(courierPaymentProcessor)
                    .preparePayment(
                            any(CourierOrder.class), eq(testUser), eq(PaymentProvider.KAKAOPAY));
        }

        @Test
        @DisplayName("동일 멱등성 키로 재요청 시 PENDING_PAYMENT 상태면 기존 주문에 대해 PG 준비 재호출")
        void ready_idempotency_pendingPayment_returnsPreparePayment() {
            // arrange
            String idempotencyKey = "idem-key-001";
            CourierOrderReadyRequest request = createReadyRequest(1L, 2, idempotencyKey);

            CourierOrder existingOrder =
                    createOrder(CourierOrderStatus.PENDING_PAYMENT, "C-26021400-ABCD2", testUser);
            CourierOrderReadyResponse expectedResponse =
                    CourierOrderReadyResponse.builder()
                            .displayCode("C-26021400-ABCD2")
                            .redirectUrl("https://kakao.pay/redirect")
                            .build();

            given(userService.findByUId(testUid)).willReturn(testUser);
            given(courierOrderService.findByIdempotencyKey(testUser, idempotencyKey))
                    .willReturn(Optional.of(existingOrder));
            given(courierPaymentProcessor.preparePayment(
                            existingOrder, testUser, PaymentProvider.KAKAOPAY))
                    .willReturn(expectedResponse);

            // act
            CourierOrderReadyResponse result = courierOrderAppService.ready(testUid, request);

            // assert
            assertThat(result.displayCode()).isEqualTo("C-26021400-ABCD2");
            verify(courierOrderService, never()).save(any());
            verify(courierPaymentProcessor)
                    .preparePayment(existingOrder, testUser, PaymentProvider.KAKAOPAY);
        }

        @Test
        @DisplayName("동일 멱등성 키로 재요청 시 PENDING_PAYMENT가 아니면 리다이렉트 응답 반환")
        void ready_idempotency_nonPending_returnsRedirect() {
            // arrange
            String idempotencyKey = "idem-key-001";
            CourierOrderReadyRequest request = createReadyRequest(1L, 2, idempotencyKey);

            CourierOrder existingOrder =
                    createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD2", testUser);

            given(userService.findByUId(testUid)).willReturn(testUser);
            given(courierOrderService.findByIdempotencyKey(testUser, idempotencyKey))
                    .willReturn(Optional.of(existingOrder));

            // act
            CourierOrderReadyResponse result = courierOrderAppService.ready(testUid, request);

            // assert
            assertThat(result.displayCode()).isEqualTo("C-26021400-ABCD2");
            assertThat(result.redirectUrl()).isEqualTo("/shop/orders/C-26021400-ABCD2");
            verify(courierOrderService, never()).save(any());
        }

        @Test
        @DisplayName("품절 상품 주문 시 예외 발생")
        void ready_soldOut_throwsException() {
            // arrange
            String idempotencyKey = "idem-key-002";
            CourierOrderReadyRequest request = createReadyRequest(1L, 10, idempotencyKey);

            CourierProduct product = createProduct("제주 감귤 5kg", new BigDecimal("15000"));
            product.toggleSoldOut(); // mark as soldOut
            ReflectionTestUtils.setField(product, "id", 1L);

            given(userService.findByUId(testUid)).willReturn(testUser);
            given(courierOrderService.findByIdempotencyKey(testUser, idempotencyKey))
                    .willReturn(Optional.empty());
            given(courierOrderService.findPendingPaymentsByUser(testUser))
                    .willReturn(List.of());
            given(courierProductService.findByIdWithLock(1L)).willReturn(product);

            // act / assert
            assertThatThrownBy(() -> courierOrderAppService.ready(testUid, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("품절된 상품");
            verify(courierOrderService, never()).save(any());
        }
    }

    // --- cancel ---

    @Nested
    @DisplayName("cancel - 주문 취소")
    class Cancel {

        @Test
        @DisplayName("PENDING_PAYMENT 상태에서 정상 취소 - 재고 복원 확인")
        void cancel_pendingPayment_success() {
            // arrange
            CourierOrder order =
                    createOrder(CourierOrderStatus.PENDING_PAYMENT, "C-26021400-ABCD2", testUser);
            CourierProduct product = createProduct("감귤", new BigDecimal("15000"));
            ReflectionTestUtils.setField(product, "id", 1L);

            CourierOrderItem item =
                    CourierOrderItem.builder()
                            .courierOrder(order)
                            .courierProduct(product)
                            .productName("감귤")
                            .productPrice(new BigDecimal("15000"))
                            .quantity(3)
                            .amount(new BigDecimal("45000"))
                            .build();
            order.getItems().add(item);

            given(userService.findByUId(testUid)).willReturn(testUser);
            given(courierOrderService.findByDisplayCodeAndUser("C-26021400-ABCD2", testUser))
                    .willReturn(order);

            // act
            courierOrderAppService.cancel(testUid, "C-26021400-ABCD2");

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.CANCELED);
            verify(courierPaymentService).markCanceled(order);
        }

        @Test
        @DisplayName("PAID 상태에서 사용자 취소 불가")
        void cancel_paidStatus_throwsException() {
            // arrange
            CourierOrder order =
                    createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD2", testUser);

            given(userService.findByUId(testUid)).willReturn(testUser);
            given(courierOrderService.findByDisplayCodeAndUser("C-26021400-ABCD2", testUser))
                    .willReturn(order);

            // act / assert
            assertThatThrownBy(() -> courierOrderAppService.cancel(testUid, "C-26021400-ABCD2"))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("이미 결제 완료된 주문");
        }
    }

    // --- fail ---

    @Nested
    @DisplayName("fail - 결제 실패 처리")
    class Fail {

        @Test
        @DisplayName("PENDING_PAYMENT 상태에서 정상 실패 처리 - 재고 복원 확인")
        void fail_pendingPayment_success() {
            // arrange
            CourierOrder order =
                    createOrder(CourierOrderStatus.PENDING_PAYMENT, "C-26021400-ABCD2", testUser);
            CourierProduct product = createProduct("감귤", new BigDecimal("15000"));
            ReflectionTestUtils.setField(product, "id", 1L);

            CourierOrderItem item =
                    CourierOrderItem.builder()
                            .courierOrder(order)
                            .courierProduct(product)
                            .productName("감귤")
                            .productPrice(new BigDecimal("15000"))
                            .quantity(3)
                            .amount(new BigDecimal("45000"))
                            .build();
            order.getItems().add(item);

            given(userService.findByUId(testUid)).willReturn(testUser);
            given(courierOrderService.findByDisplayCodeAndUser("C-26021400-ABCD2", testUser))
                    .willReturn(order);

            // act
            courierOrderAppService.fail(testUid, "C-26021400-ABCD2");

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.FAILED);
            verify(courierPaymentService).markFailed(order);
        }
    }

    // --- getOrders ---

    @Nested
    @DisplayName("getOrders - 주문 목록 조회")
    class GetOrders {

        @Test
        @DisplayName("사용자의 주문 목록 반환")
        void getOrders_success() {
            // arrange
            CourierOrder order1 =
                    createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD2", testUser);
            CourierOrder order2 =
                    createOrder(CourierOrderStatus.DELIVERED, "C-26021400-DEF34", testUser);
            ReflectionTestUtils.setField(order2, "id", 2L);

            given(userService.findByUId(testUid)).willReturn(testUser);
            given(courierOrderService.findByUserAndDateRange(
                    eq(testUser), any(java.time.LocalDateTime.class), any(java.time.LocalDateTime.class)))
                    .willReturn(List.of(order1, order2));

            // act
            List<CourierOrderResponse> result =
                    courierOrderAppService.getOrders(testUid, null, null);

            // assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).displayCode()).isEqualTo("C-26021400-ABCD2");
            assertThat(result.get(1).displayCode()).isEqualTo("C-26021400-DEF34");
        }
    }

    // --- getOrderDetail ---

    @Nested
    @DisplayName("getOrderDetail - 주문 상세 조회")
    class GetOrderDetail {

        @Test
        @DisplayName("displayCode로 주문 상세 반환")
        void getOrderDetail_success() {
            // arrange
            CourierOrder order =
                    createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD2", testUser);

            given(userService.findByUId(testUid)).willReturn(testUser);
            given(courierOrderService.findByDisplayCodeAndUser("C-26021400-ABCD2", testUser))
                    .willReturn(order);

            // act
            CourierOrderDetailResponse result =
                    courierOrderAppService.getOrderDetail(testUid, "C-26021400-ABCD2");

            // assert
            assertThat(result).isNotNull();
            assertThat(result.displayCode()).isEqualTo("C-26021400-ABCD2");
            assertThat(result.status()).isEqualTo(CourierOrderStatus.PAID);
        }

        @Test
        @DisplayName("다른 사용자의 주문 조회 시 NotFoundException")
        void getOrderDetail_otherUser_throwsException() {
            // arrange
            given(userService.findByUId(testUid)).willReturn(testUser);
            given(courierOrderService.findByDisplayCodeAndUser("C-26021400-ABCD2", testUser))
                    .willThrow(new NotFoundException("존재하지 않는 택배 주문입니다."));

            // act / assert
            assertThatThrownBy(
                            () ->
                                    courierOrderAppService.getOrderDetail(
                                            testUid, "C-26021400-ABCD2"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("존재하지 않는 택배 주문");
        }
    }
}
