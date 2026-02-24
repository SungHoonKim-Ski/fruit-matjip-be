package store.onuljang.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
import store.onuljang.shared.entity.enums.CourierOrderStatus;
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

    // --- getOrders ---

    @Nested
    @DisplayName("getOrders - 관리자 주문 목록 조회")
    class GetOrders {

        @Test
        @DisplayName("전체 주문 목록을 반환한다")
        void getOrders_success() {
            // arrange
            CourierOrder order1 = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD1");
            CourierOrder order2 = createOrder(CourierOrderStatus.SHIPPED, "C-26021400-ABCD2");
            ReflectionTestUtils.setField(order2, "id", 2L);

            Page<CourierOrder> page =
                    new PageImpl<>(List.of(order1, order2), PageRequest.of(0, 50), 2);
            given(courierOrderService.findAllByStatus(null, 0, 50)).willReturn(page);

            // act
            AdminCourierOrderListResponse result =
                    courierAdminOrderAppService.getOrders(null, null, 0, 50);

            // assert
            assertThat(result.orders()).hasSize(2);
            assertThat(result.orders().get(0).displayCode()).isEqualTo("C-26021400-ABCD1");
            assertThat(result.orders().get(1).displayCode()).isEqualTo("C-26021400-ABCD2");
        }

        @Test
        @DisplayName("운송장 다운 여부 필터로 주문 목록을 반환한다")
        void getOrders_withWaybillDownloadedFilter() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD1");

            Page<CourierOrder> page =
                    new PageImpl<>(List.of(order), PageRequest.of(0, 50), 1);
            given(courierOrderService.findAllByStatusAndWaybillDownloaded(null, false, 0, 50))
                    .willReturn(page);

            // act
            AdminCourierOrderListResponse result =
                    courierAdminOrderAppService.getOrders(null, false, 0, 50);

            // assert
            assertThat(result.orders()).hasSize(1);
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
                    courierAdminOrderAppService.getOrders(CourierOrderStatus.PAID, null, 0, 50);

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
        @DisplayName("PAID → PREPARING 상태 변경 성공")
        void updateStatus_paidToPreparing() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.updateStatus(1L, CourierOrderStatus.PREPARING);

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.PREPARING);
        }

        @Test
        @DisplayName("PAID → SHIPPED 상태 변경 성공")
        void updateStatus_paidToShipped() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.updateStatus(1L, CourierOrderStatus.SHIPPED);

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.SHIPPED);
        }

        @Test
        @DisplayName("PREPARING → SHIPPED 상태 변경 성공")
        void updateStatus_preparingToShipped() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PREPARING, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.updateStatus(1L, CourierOrderStatus.SHIPPED);

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.SHIPPED);
        }

        @Test
        @DisplayName("SHIPPED → IN_TRANSIT 상태 변경 성공")
        void updateStatus_shippedToInTransit() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.SHIPPED, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.updateStatus(1L, CourierOrderStatus.IN_TRANSIT);

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.IN_TRANSIT);
        }

        @Test
        @DisplayName("SHIPPED → DELIVERED 상태 변경 성공")
        void updateStatus_shippedToDelivered() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.SHIPPED, "C-26021400-ABCD1");
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
        @DisplayName("잘못된 상태 전이 시 AdminValidateException 발생 - PENDING_PAYMENT → PREPARING 불가")
        void updateStatus_invalidTransition_pendingToPreparing() {
            // arrange
            CourierOrder order =
                    createOrder(CourierOrderStatus.PENDING_PAYMENT, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act / assert
            assertThatThrownBy(
                            () ->
                                    courierAdminOrderAppService.updateStatus(
                                            1L, CourierOrderStatus.PREPARING))
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
                    .hasMessageContaining("발송완료 또는 배송중 상태에서만");
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
                    .hasMessageContaining("발송완료 상태에서만");
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
        @DisplayName("PAID 상태에서 운송장 번호와 함께 발송 처리 성공")
        void ship_fromPaid_success() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.ship(1L, "1234567890");

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.SHIPPED);
            assertThat(order.getWaybillNumber()).isEqualTo("1234567890");
        }

        @Test
        @DisplayName("PREPARING 상태에서 발송 처리 성공")
        void ship_fromPreparing_success() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PREPARING, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act
            courierAdminOrderAppService.ship(1L, "1234567890");

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.SHIPPED);
            assertThat(order.getWaybillNumber()).isEqualTo("1234567890");
        }

        @Test
        @DisplayName("SHIPPED 상태에서 발송 처리 시 예외 발생")
        void ship_fromShipped_throwsException() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.SHIPPED, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act / assert
            assertThatThrownBy(() -> courierAdminOrderAppService.ship(1L, "1234567890"))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("결제완료 또는 준비중 상태에서만");
        }

        @Test
        @DisplayName("DELIVERED 상태에서 발송 처리 시 예외 발생")
        void ship_fromDelivered_throwsException() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.DELIVERED, "C-26021400-ABCD1");
            given(courierOrderService.findById(1L)).willReturn(order);

            // act / assert
            assertThatThrownBy(() -> courierAdminOrderAppService.ship(1L, "1234567890"))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("결제완료 또는 준비중 상태에서만");
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
        @DisplayName("PREPARING 상태에서 취소 - PG 환불 + 재고 복원")
        void cancel_preparing_success() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PREPARING, "C-26021400-ABCD1");
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
        @DisplayName("SHIPPED 상태에서 취소 시 예외 발생")
        void cancel_shipped_throwsException() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.SHIPPED, "C-26021400-ABCD1");
            given(courierOrderService.findByIdWithItems(1L)).willReturn(order);

            // act / assert
            assertThatThrownBy(() -> courierAdminOrderAppService.cancel(1L))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("이미 발송/배송완료/취소된 주문");
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
                    .hasMessageContaining("이미 발송/배송완료/취소된 주문");
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
                    .hasMessageContaining("이미 발송/배송완료/취소된 주문");
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
            assertThat(order.getWaybillDownloadedAt()).isNotNull();
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
