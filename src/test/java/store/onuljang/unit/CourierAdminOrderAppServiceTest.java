package store.onuljang.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import store.onuljang.courier.appservice.CourierAdminOrderAppService;
import store.onuljang.courier.dto.AdminCourierOrderDetailResponse;
import store.onuljang.courier.dto.AdminCourierOrderListResponse;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierOrderItem;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.service.CourierOrderService;
import store.onuljang.courier.service.CourierPaymentService;
import store.onuljang.courier.service.CourierRefundService;
import store.onuljang.courier.service.WaybillExcelService;
import store.onuljang.shared.entity.enums.CourierCompany;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.entity.enums.UserPointTransactionType;
import store.onuljang.shared.exception.AdminValidateException;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shop.admin.entity.Admin;

@ExtendWith(MockitoExtension.class)
class CourierAdminOrderAppServiceTest {

    @InjectMocks private CourierAdminOrderAppService courierAdminOrderAppService;

    @Mock private CourierOrderService courierOrderService;
    @Mock private CourierPaymentService courierPaymentService;
    @Mock private CourierRefundService courierRefundService;
    @Mock private WaybillExcelService waybillExcelService;
    @Mock private store.onuljang.courier.service.TrackingUploadService trackingUploadService;
    @Mock private store.onuljang.shared.user.service.UserPointService userPointService;
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;

    private Users testUser;

    @BeforeEach
    void setUp() {
        testUser =
                Users.builder()
                        .socialId("social123")
                        .name("테스트유저")
                        .uid(UUID.randomUUID())
                        .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);
    }

    private CourierOrder createOrder(CourierOrderStatus status, String displayCode) {
        CourierOrder order =
                CourierOrder.builder()
                        .user(testUser)
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

    private CourierProduct createProduct(String name) {
        Admin admin =
                Admin.builder()
                        .name("관리자")
                        .email("admin@test.com")
                        .password("password")
                        .build();
        return CourierProduct.builder()
                .name(name)
                .productUrl("https://example.com/img.jpg")
                .price(new BigDecimal("15000"))
                .visible(true)
                .registeredAdmin(admin)
                .build();
    }

    private CourierOrderItem createOrderItem(
            CourierOrder order, CourierProduct product, int quantity) {
        CourierOrderItem item =
                CourierOrderItem.builder()
                        .courierOrder(order)
                        .courierProduct(product)
                        .productName(product.getName())
                        .productPrice(product.getPrice())
                        .quantity(quantity)
                        .amount(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                        .build();
        order.getItems().add(item);
        return item;
    }

    private CourierOrder createOrderWithPointUsed(
            CourierOrderStatus status, String displayCode, BigDecimal pointUsed) {
        CourierOrder order =
                CourierOrder.builder()
                        .user(testUser)
                        .displayCode(displayCode)
                        .status(status)
                        .receiverName("홍길동")
                        .receiverPhone("010-1234-5678")
                        .postalCode("06134")
                        .address1("서울시 강남구")
                        .productAmount(BigDecimal.valueOf(30000))
                        .shippingFee(BigDecimal.valueOf(4000))
                        .totalAmount(BigDecimal.valueOf(34000))
                        .pointUsed(pointUsed)
                        .build();
        ReflectionTestUtils.setField(order, "id", 1L);
        return order;
    }

    // --- getOrders ---

    @Nested
    @DisplayName("getOrders - 관리자 주문 목록 조회")
    class GetOrders {

        @Test
        @DisplayName("전체 주문 목록을 반환한다")
        void getOrders_success() {
            // arrange
            CourierOrder order1 = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD1");
            CourierOrder order2 = createOrder(CourierOrderStatus.ORDER_COMPLETED, "C-26021400-ABCD2");
            ReflectionTestUtils.setField(order2, "id", 2L);

            Page<CourierOrder> page =
                    new PageImpl<>(List.of(order1, order2), PageRequest.of(0, 50), 2);
            given(courierOrderService.findAllByStatus(null, 0, 50)).willReturn(page);

            // act
            AdminCourierOrderListResponse result =
                    courierAdminOrderAppService.getOrders(null, 0, 50);

            // assert
            assertThat(result.orders()).hasSize(2);
            assertThat(result.orders().get(0).displayCode()).isEqualTo("C-26021400-ABCD1");
            assertThat(result.orders().get(1).displayCode()).isEqualTo("C-26021400-ABCD2");
        }

        @Test
        @DisplayName("상태 필터로 주문 목록을 반환한다")
        void getOrders_withStatusFilter() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD1");

            Page<CourierOrder> page =
                    new PageImpl<>(List.of(order), PageRequest.of(0, 50), 1);
            given(courierOrderService.findAllByStatus(CourierOrderStatus.PAID, 0, 50))
                    .willReturn(page);

            // act
            AdminCourierOrderListResponse result =
                    courierAdminOrderAppService.getOrders(CourierOrderStatus.PAID, 0, 50);

            // assert
            assertThat(result.orders()).hasSize(1);
            assertThat(result.orders().get(0).status()).isEqualTo(CourierOrderStatus.PAID);
        }
    }

    // --- getOrder ---

    @Nested
    @DisplayName("getOrder - 관리자 주문 상세 조회")
    class GetOrder {

        @Test
        @DisplayName("주문 상세를 반환한다")
        void getOrder_success() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD1");

            given(courierOrderService.findByIdWithItems(1L)).willReturn(order);

            // act
            AdminCourierOrderDetailResponse result = courierAdminOrderAppService.getOrder(1L);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.displayCode()).isEqualTo("C-26021400-ABCD1");
            assertThat(result.status()).isEqualTo(CourierOrderStatus.PAID);
            assertThat(result.receiverName()).isEqualTo("홍길동");
        }
    }

    // --- updateStatus ---

    @Nested
    @DisplayName("updateStatus - 관리자 상태 변경")
    class UpdateStatus {

        @Test
        @DisplayName("PAID → ORDERING 상태 변경 성공")
        void updateStatus_paidToOrdering() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.updateStatus(1L, CourierOrderStatus.ORDERING);

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.ORDERING);
        }

        @Test
        @DisplayName("PAID → ORDER_COMPLETED 상태 변경 성공")
        void updateStatus_paidToOrderCompleted() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.updateStatus(1L, CourierOrderStatus.ORDER_COMPLETED);

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.ORDER_COMPLETED);
        }

        @Test
        @DisplayName("ORDERING → ORDER_COMPLETED 상태 변경 성공")
        void updateStatus_orderingToOrderCompleted() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.ORDERING, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.updateStatus(1L, CourierOrderStatus.ORDER_COMPLETED);

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.ORDER_COMPLETED);
        }

        @Test
        @DisplayName("ORDER_COMPLETED → IN_TRANSIT 상태 변경 성공")
        void updateStatus_orderCompletedToInTransit() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.ORDER_COMPLETED, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.updateStatus(1L, CourierOrderStatus.IN_TRANSIT);

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.IN_TRANSIT);
        }

        @Test
        @DisplayName("ORDER_COMPLETED → DELIVERED 상태 변경 성공")
        void updateStatus_orderCompletedToDelivered() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.ORDER_COMPLETED, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.updateStatus(1L, CourierOrderStatus.DELIVERED);

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.DELIVERED);
        }

        @Test
        @DisplayName("IN_TRANSIT → DELIVERED 상태 변경 성공")
        void updateStatus_inTransitToDelivered() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.IN_TRANSIT, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.updateStatus(1L, CourierOrderStatus.DELIVERED);

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.DELIVERED);
        }

        @Test
        @DisplayName("잘못된 상태 전이 시 AdminValidateException 발생 - PENDING_PAYMENT → ORDERING 불가")
        void updateStatus_invalidTransition_pendingToPreparing() {
            // arrange
            CourierOrder order =
                    createOrder(CourierOrderStatus.PENDING_PAYMENT, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act / assert
            assertThatThrownBy(
                            () ->
                                    courierAdminOrderAppService.updateStatus(
                                            1L, CourierOrderStatus.ORDERING))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("결제완료 상태에서만");
        }

        @Test
        @DisplayName("잘못된 상태 전이 시 AdminValidateException 발생 - PAID → DELIVERED 불가")
        void updateStatus_invalidTransition_paidToDelivered() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act / assert
            assertThatThrownBy(
                            () ->
                                    courierAdminOrderAppService.updateStatus(
                                            1L, CourierOrderStatus.DELIVERED))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("발주완료 또는 배송중 상태에서만");
        }

        @Test
        @DisplayName("잘못된 상태 전이 시 AdminValidateException 발생 - PAID → IN_TRANSIT 불가")
        void updateStatus_invalidTransition_paidToInTransit() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act / assert
            assertThatThrownBy(
                            () ->
                                    courierAdminOrderAppService.updateStatus(
                                            1L, CourierOrderStatus.IN_TRANSIT))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("발주완료 상태에서만");
        }

        @Test
        @DisplayName("CANCELED 상태로 변경 불가 - updateStatus는 cancel() 사용")
        void updateStatus_invalidTransition_toCanceled() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act / assert
            assertThatThrownBy(
                            () ->
                                    courierAdminOrderAppService.updateStatus(
                                            1L, CourierOrderStatus.CANCELED))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("변경할 수 없는 상태");
        }
    }

    // --- ship ---

    @Nested
    @DisplayName("ship - 발송 처리")
    class Ship {

        @Test
        @DisplayName("PAID 상태에서 운송장 번호와 택배사(LOGEN)와 함께 발송 처리 성공")
        void ship_fromPaid_success() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.ship(1L, "1234567890", CourierCompany.LOGEN);

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.ORDER_COMPLETED);
            assertThat(order.getWaybillNumber()).isEqualTo("1234567890");
            assertThat(order.getCourierCompany()).isEqualTo(CourierCompany.LOGEN);
        }

        @Test
        @DisplayName("ORDERING 상태에서 CJ 택배사로 발주완료 처리 성공")
        void ship_fromOrdering_withCj_success() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.ORDERING, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.ship(1L, "9876543210", CourierCompany.CJ);

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.ORDER_COMPLETED);
            assertThat(order.getWaybillNumber()).isEqualTo("9876543210");
            assertThat(order.getCourierCompany()).isEqualTo(CourierCompany.CJ);
        }

        @Test
        @DisplayName("PAID 상태에서 LOTTE 택배사로 발송 처리 성공")
        void ship_fromPaid_withLotte_success() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.ship(1L, "1111222233", CourierCompany.LOTTE);

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.ORDER_COMPLETED);
            assertThat(order.getCourierCompany()).isEqualTo(CourierCompany.LOTTE);
        }

        @Test
        @DisplayName("ORDER_COMPLETED 상태에서 발주완료 처리 시 예외 발생")
        void ship_fromOrderCompleted_throwsException() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.ORDER_COMPLETED, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act / assert
            assertThatThrownBy(
                            () ->
                                    courierAdminOrderAppService.ship(
                                            1L, "1234567890", CourierCompany.LOGEN))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("결제완료 또는 발주중 상태에서만");
        }

        @Test
        @DisplayName("DELIVERED 상태에서 발송 처리 시 예외 발생")
        void ship_fromDelivered_throwsException() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.DELIVERED, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act / assert
            assertThatThrownBy(
                            () ->
                                    courierAdminOrderAppService.ship(
                                            1L, "1234567890", CourierCompany.HANJIN))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("결제완료 또는 발주중 상태에서만");
        }
    }

    // --- cancel ---

    @Nested
    @DisplayName("cancel - 관리자 주문 취소")
    class Cancel {

        @Test
        @DisplayName("PAID 상태에서 취소 - PG 환불 + 재고 복원")
        void cancel_paid_success() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD1");
            ReflectionTestUtils.setField(order, "pgTid", "T_PG_TID_001");
            CourierProduct product = createProduct("감귤");
            ReflectionTestUtils.setField(product, "id", 1L);
            createOrderItem(order, product, 3);

            given(courierOrderService.findByIdWithItems(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.cancel(1L);

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.CANCELED);
            verify(courierPaymentService).markCanceled(order);
            verify(courierRefundService).refund(order, order.getTotalAmount());
        }

        @Test
        @DisplayName("ORDERING 상태에서 취소 - PG 환불 + 재고 복원")
        void cancel_ordering_success() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.ORDERING, "C-26021400-ABCD1");
            ReflectionTestUtils.setField(order, "pgTid", "T_PG_TID_002");
            CourierProduct product = createProduct("감귤");
            ReflectionTestUtils.setField(product, "id", 1L);
            createOrderItem(order, product, 3);

            given(courierOrderService.findByIdWithItems(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.cancel(1L);

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.CANCELED);
            verify(courierPaymentService).markCanceled(order);
            verify(courierRefundService).refund(order, order.getTotalAmount());
        }

        @Test
        @DisplayName("PAID 포인트 사용 주문 취소 시 PG는 pgPaymentAmount로 환불하고 포인트를 환원한다")
        void cancel_paid_withPointUsed_refundsPgPaymentAmountAndRestoresPoints() {
            // arrange
            CourierOrder order = createOrderWithPointUsed(
                    CourierOrderStatus.PAID, "C-26021400-ABCD1", BigDecimal.valueOf(5000));
            ReflectionTestUtils.setField(order, "pgTid", "T_PG_TID_003");
            CourierProduct product = createProduct("감귤");
            ReflectionTestUtils.setField(product, "id", 1L);
            createOrderItem(order, product, 3);

            given(courierOrderService.findByIdWithItems(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.cancel(1L);

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.CANCELED);
            // PG 환불은 pgPaymentAmount(29000)로 호출 (totalAmount(34000)이 아님)
            org.mockito.ArgumentCaptor<BigDecimal> refundCaptor =
                    org.mockito.ArgumentCaptor.forClass(BigDecimal.class);
            verify(courierRefundService).refund(eq(order), refundCaptor.capture());
            assertThat(refundCaptor.getValue()).isEqualByComparingTo(BigDecimal.valueOf(29000));
            // 포인트 환원 호출 확인
            verify(userPointService).earn(
                    eq(testUser.getUid()), eq(BigDecimal.valueOf(5000)),
                    eq(UserPointTransactionType.CANCEL_USE),
                    any(), any(), any(), any());
        }

        @Test
        @DisplayName("ORDERING 포인트 사용 주문 취소 시 PG는 pgPaymentAmount로 환불하고 포인트를 환원한다")
        void cancel_ordering_withPointUsed_refundsPgPaymentAmountAndRestoresPoints() {
            // arrange
            CourierOrder order = createOrderWithPointUsed(
                    CourierOrderStatus.ORDERING, "C-26021400-ABCD1", BigDecimal.valueOf(5000));
            ReflectionTestUtils.setField(order, "pgTid", "T_PG_TID_004");
            CourierProduct product = createProduct("감귤");
            ReflectionTestUtils.setField(product, "id", 1L);
            createOrderItem(order, product, 3);

            given(courierOrderService.findByIdWithItems(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.cancel(1L);

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.CANCELED);
            org.mockito.ArgumentCaptor<BigDecimal> refundCaptor =
                    org.mockito.ArgumentCaptor.forClass(BigDecimal.class);
            verify(courierRefundService).refund(eq(order), refundCaptor.capture());
            assertThat(refundCaptor.getValue()).isEqualByComparingTo(BigDecimal.valueOf(29000));
            verify(userPointService).earn(
                    eq(testUser.getUid()), eq(BigDecimal.valueOf(5000)),
                    eq(UserPointTransactionType.CANCEL_USE),
                    any(), any(), any(), any());
        }

        @Test
        @DisplayName("전액 포인트 결제 주문 취소 시 PG 환불 없이 포인트만 환원한다")
        void cancel_paid_fullPointPayment_noRefundButRestoresPoints() {
            // arrange
            CourierOrder order = createOrderWithPointUsed(
                    CourierOrderStatus.PAID, "C-26021400-ABCD1", BigDecimal.valueOf(34000));
            CourierProduct product = createProduct("감귤");
            ReflectionTestUtils.setField(product, "id", 1L);
            createOrderItem(order, product, 3);

            given(courierOrderService.findByIdWithItems(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.cancel(1L);

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.CANCELED);
            // pgPaymentAmount = 0 이므로 PG 환불 미호출
            verify(courierRefundService, never()).refund(any(), any());
            // 포인트 전액 환원
            verify(userPointService).earn(
                    eq(testUser.getUid()), eq(BigDecimal.valueOf(34000)),
                    eq(UserPointTransactionType.CANCEL_USE),
                    any(), any(), any(), any());
        }

        @Test
        @DisplayName("PENDING_PAYMENT 상태에서 취소 - PG 환불 없이 재고 복원")
        void cancel_pendingPayment_success() {
            // arrange
            CourierOrder order =
                    createOrder(CourierOrderStatus.PENDING_PAYMENT, "C-26021400-ABCD1");
            CourierProduct product = createProduct("감귤");
            ReflectionTestUtils.setField(product, "id", 1L);
            createOrderItem(order, product, 2);

            given(courierOrderService.findByIdWithItems(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.cancel(1L);

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.CANCELED);
            verify(courierPaymentService).markCanceled(order);
            verify(courierRefundService, never()).refund(any(), any());
        }

        @Test
        @DisplayName("ORDER_COMPLETED 상태에서 취소 시 예외 발생")
        void cancel_orderCompleted_throwsException() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.ORDER_COMPLETED, "C-26021400-ABCD1");
            given(courierOrderService.findByIdWithItems(1L)).willReturn(order);

            // act / assert
            assertThatThrownBy(() -> courierAdminOrderAppService.cancel(1L))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("발주완료 이후 또는 취소된 주문");
        }

        @Test
        @DisplayName("DELIVERED 상태에서 취소 시 예외 발생")
        void cancel_delivered_throwsException() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.DELIVERED, "C-26021400-ABCD1");
            given(courierOrderService.findByIdWithItems(1L)).willReturn(order);

            // act / assert
            assertThatThrownBy(() -> courierAdminOrderAppService.cancel(1L))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("발주완료 이후 또는 취소된 주문");
        }

        @Test
        @DisplayName("CANCELED 상태에서 취소 시 예외 발생")
        void cancel_alreadyCanceled_throwsException() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.CANCELED, "C-26021400-ABCD1");
            given(courierOrderService.findByIdWithItems(1L)).willReturn(order);

            // act / assert
            assertThatThrownBy(() -> courierAdminOrderAppService.cancel(1L))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("발주완료 이후 또는 취소된 주문");
        }

        @Test
        @DisplayName("PAID 상태에서 PG 환불 실패 시 AdminValidateException 발생하고 취소 미처리")
        void cancel_paid_refundFails_throwsException() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD1");
            ReflectionTestUtils.setField(order, "pgTid", "T_PG_TID_001");
            CourierProduct product = createProduct("감귤");
            ReflectionTestUtils.setField(product, "id", 1L);
            createOrderItem(order, product, 2);

            given(courierOrderService.findByIdWithItems(1L)).willReturn(order);
            doThrow(new RuntimeException("PG 통신 오류"))
                    .when(courierRefundService)
                    .refund(any(), any());

            // act / assert
            assertThatThrownBy(() -> courierAdminOrderAppService.cancel(1L))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("PG 환불에 실패했습니다");

            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.PAID);
            verify(courierPaymentService, never()).markCanceled(any());
        }
    }

    // --- downloadWaybillExcel ---

    @Nested
    @DisplayName("downloadWaybillExcel - 운송장 Excel 다운로드")
    class DownloadWaybillExcel {

        @Test
        @DisplayName("단건 운송장 Excel 다운로드 성공 및 다운로드 마킹")
        void downloadWaybillExcel_success() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD1");
            byte[] expectedBytes = new byte[] {1, 2, 3};

            given(courierOrderService.findByIdWithItems(1L)).willReturn(order);
            given(waybillExcelService.generateWaybillExcel(order)).willReturn(expectedBytes);

            // act
            byte[] result = courierAdminOrderAppService.downloadWaybillExcel(1L);

            // assert
            assertThat(result).isEqualTo(expectedBytes);
        }

        @Test
        @DisplayName("대량 운송장 Excel 다운로드 성공")
        void downloadWaybillExcelBulk_success() {
            // arrange
            List<Long> orderIds = List.of(1L, 2L);
            CourierOrder order1 = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD1");
            CourierOrder order2 = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD2");
            ReflectionTestUtils.setField(order2, "id", 2L);
            List<CourierOrder> orders = List.of(order1, order2);
            byte[] expectedBytes = new byte[] {4, 5, 6};

            given(courierOrderService.findAllByIds(orderIds)).willReturn(orders);
            given(waybillExcelService.generateWaybillExcel(orders)).willReturn(expectedBytes);

            // act
            byte[] result = courierAdminOrderAppService.downloadWaybillExcelBulk(orderIds);

            // assert
            assertThat(result).isEqualTo(expectedBytes);
        }
    }
}
