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
import store.onuljang.courier.appservice.CourierClaimAppService;
import store.onuljang.courier.dto.CourierClaimApproveRequest;
import store.onuljang.courier.dto.CourierClaimListResponse;
import store.onuljang.courier.dto.CourierClaimRequest;
import store.onuljang.courier.dto.CourierClaimResponse;
import store.onuljang.courier.entity.CourierClaim;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierOrderItem;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.service.CourierClaimService;
import store.onuljang.courier.service.CourierOrderService;
import store.onuljang.courier.service.CourierRefundService;
import store.onuljang.shared.entity.enums.CourierClaimStatus;
import store.onuljang.shared.entity.enums.CourierClaimType;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.entity.enums.ShippingFeeBearer;
import store.onuljang.shared.exception.AdminValidateException;
import store.onuljang.shared.exception.UserValidateException;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.user.service.UserService;
import store.onuljang.shop.admin.entity.Admin;

@ExtendWith(MockitoExtension.class)
class CourierClaimAppServiceTest {

    @InjectMocks private CourierClaimAppService courierClaimAppService;

    @Mock private UserService userService;
    @Mock private CourierOrderService courierOrderService;
    @Mock private CourierClaimService courierClaimService;
    @Mock private CourierRefundService courierRefundService;

    private Users testUser;
    private String testUid;

    @BeforeEach
    void setUp() {
        testUser =
                Users.builder()
                        .socialId("social123")
                        .name("테스트유저")
                        .uid(UUID.randomUUID())
                        .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);
        testUid = testUser.getUid().toString();
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

    private CourierOrderItem createOrderItem(CourierOrder order, String productName, int quantity) {
        Admin admin =
                Admin.builder()
                        .name("관리자")
                        .email("admin@test.com")
                        .password("password")
                        .build();
        CourierProduct product =
                CourierProduct.builder()
                        .name(productName)
                        .productUrl("https://example.com/img.jpg")
                        .price(BigDecimal.valueOf(15000))
                        .visible(true)
                        .registeredAdmin(admin)
                        .build();
        ReflectionTestUtils.setField(product, "id", 20L);
        CourierOrderItem item =
                CourierOrderItem.builder()
                        .courierOrder(order)
                        .courierProduct(product)
                        .productName(productName)
                        .productPrice(BigDecimal.valueOf(15000))
                        .quantity(quantity)
                        .amount(BigDecimal.valueOf(15000).multiply(BigDecimal.valueOf(quantity)))
                        .build();
        ReflectionTestUtils.setField(item, "id", 10L);
        order.getItems().add(item);
        return item;
    }

    private CourierClaim createClaim(
            CourierOrder order,
            CourierOrderItem item,
            CourierClaimStatus status) {
        CourierClaim claim =
                CourierClaim.builder()
                        .courierOrder(order)
                        .courierOrderItem(item)
                        .claimType(CourierClaimType.QUALITY_ISSUE)
                        .claimStatus(status)
                        .reason("과일 상태 불량")
                        .returnShippingFeeBearer(ShippingFeeBearer.SELLER)
                        .build();
        ReflectionTestUtils.setField(claim, "id", 100L);
        return claim;
    }

    // === createClaim ===

    @Nested
    @DisplayName("createClaim - 사용자 클레임 접수")
    class CreateClaim {

        @Test
        @DisplayName("DELIVERED 주문에 대해 클레임 접수 성공")
        void createClaim_delivered_success() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.DELIVERED, "C-26021400-ABCD1");
            CourierClaimRequest request =
                    new CourierClaimRequest(CourierClaimType.QUALITY_ISSUE, null, "과일 상태 불량");

            given(userService.findByUId(testUid)).willReturn(testUser);
            given(courierOrderService.findByDisplayCodeAndUser("C-26021400-ABCD1", testUser))
                    .willReturn(order);
            given(courierClaimService.save(any(CourierClaim.class)))
                    .willAnswer(invocation -> {
                        CourierClaim c = invocation.getArgument(0);
                        ReflectionTestUtils.setField(c, "id", 100L);
                        return c;
                    });

            // act
            CourierClaimResponse result =
                    courierClaimAppService.createClaim(testUid, "C-26021400-ABCD1", request);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.claimType()).isEqualTo(CourierClaimType.QUALITY_ISSUE);
            assertThat(result.claimStatus()).isEqualTo(CourierClaimStatus.REQUESTED);
            assertThat(result.returnShippingFeeBearer()).isEqualTo(ShippingFeeBearer.SELLER);
            verify(courierClaimService).save(any(CourierClaim.class));
        }

        @Test
        @DisplayName("결제 전 상태(PENDING_PAYMENT)에서 UserValidateException 발생")
        void createClaim_pendingPayment_throwsException() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PENDING_PAYMENT, "C-26021400-ABCD1");
            CourierClaimRequest request =
                    new CourierClaimRequest(CourierClaimType.QUALITY_ISSUE, null, "과일 상태 불량");

            given(userService.findByUId(testUid)).willReturn(testUser);
            given(courierOrderService.findByDisplayCodeAndUser("C-26021400-ABCD1", testUser))
                    .willReturn(order);

            // act / assert
            assertThatThrownBy(
                            () ->
                                    courierClaimAppService.createClaim(
                                            testUid, "C-26021400-ABCD1", request))
                    .isInstanceOf(UserValidateException.class)
                    .hasMessageContaining("결제 완료 후 문의가 가능합니다.");
            verify(courierClaimService, never()).save(any());
        }

        @Test
        @DisplayName("특정 주문 아이템 지정 시 해당 아이템 CLAIM_REQUESTED 상태로 변경")
        void createClaim_withOrderItem_setsClaimRequested() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.DELIVERED, "C-26021400-ABCD1");
            CourierOrderItem item = createOrderItem(order, "감귤", 2);
            CourierClaimRequest request =
                    new CourierClaimRequest(CourierClaimType.QUALITY_ISSUE, 20L, "과일 상태 불량");

            given(userService.findByUId(testUid)).willReturn(testUser);
            given(courierOrderService.findByDisplayCodeAndUser("C-26021400-ABCD1", testUser))
                    .willReturn(order);
            given(courierClaimService.save(any(CourierClaim.class)))
                    .willAnswer(invocation -> {
                        CourierClaim c = invocation.getArgument(0);
                        ReflectionTestUtils.setField(c, "id", 100L);
                        return c;
                    });

            // act
            CourierClaimResponse result =
                    courierClaimAppService.createClaim(testUid, "C-26021400-ABCD1", request);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.orderItemId()).isEqualTo(10L);
            assertThat(item.getItemStatus())
                    .isEqualTo(
                            store.onuljang.shared.entity.enums.CourierOrderItemStatus
                                    .CLAIM_REQUESTED);
        }

        @Test
        @DisplayName("CHANGE_OF_MIND 클레임 시 bearer=CUSTOMER로 설정")
        void createClaim_changeOfMind_setBearerCustomer() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.DELIVERED, "C-26021400-ABCD1");
            CourierClaimRequest request =
                    new CourierClaimRequest(CourierClaimType.CHANGE_OF_MIND, null, "단순 변심");

            given(userService.findByUId(testUid)).willReturn(testUser);
            given(courierOrderService.findByDisplayCodeAndUser("C-26021400-ABCD1", testUser))
                    .willReturn(order);
            given(courierClaimService.save(any(CourierClaim.class)))
                    .willAnswer(invocation -> {
                        CourierClaim c = invocation.getArgument(0);
                        ReflectionTestUtils.setField(c, "id", 100L);
                        return c;
                    });

            // act
            CourierClaimResponse result =
                    courierClaimAppService.createClaim(testUid, "C-26021400-ABCD1", request);

            // assert
            assertThat(result.claimType()).isEqualTo(CourierClaimType.CHANGE_OF_MIND);
            assertThat(result.returnShippingFeeBearer()).isEqualTo(ShippingFeeBearer.CUSTOMER);
        }
    }

    // === approveClaim ===

    @Nested
    @DisplayName("approveClaim - 관리자 클레임 승인")
    class ApproveClaim {

        @Test
        @DisplayName("REFUND 액션으로 승인 시 환불 처리 및 RESOLVED 상태")
        void approveClaim_refund_success() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.DELIVERED, "C-26021400-ABCD1");
            ReflectionTestUtils.setField(order, "pgTid", "T_PG_TID_001");
            CourierOrderItem item = createOrderItem(order, "감귤", 2);
            CourierClaim claim = createClaim(order, item, CourierClaimStatus.REQUESTED);
            CourierClaimApproveRequest request =
                    new CourierClaimApproveRequest(
                            "REFUND", "환불 승인합니다", BigDecimal.valueOf(15000));

            given(courierClaimService.findById(100L)).willReturn(claim);

            // act
            CourierClaimResponse result =
                    courierClaimAppService.approveClaim(100L, request);

            // assert
            assertThat(result.claimStatus()).isEqualTo(CourierClaimStatus.RESOLVED);
            assertThat(result.adminNote()).isEqualTo("환불 승인합니다");
            assertThat(result.refundAmount()).isEqualByComparingTo(BigDecimal.valueOf(15000));
            verify(courierRefundService).refund(order, BigDecimal.valueOf(15000));
            assertThat(item.getItemStatus())
                    .isEqualTo(
                            store.onuljang.shared.entity.enums.CourierOrderItemStatus.REFUNDED);
        }

        @Test
        @DisplayName("커스텀 refundAmount로 승인 시 해당 금액으로 환불 처리")
        void approveClaim_customRefundAmount() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.DELIVERED, "C-26021400-ABCD1");
            ReflectionTestUtils.setField(order, "pgTid", "T_PG_TID_001");
            CourierOrderItem item = createOrderItem(order, "감귤", 2);
            CourierClaim claim = createClaim(order, item, CourierClaimStatus.REQUESTED);
            CourierClaimApproveRequest request =
                    new CourierClaimApproveRequest(
                            "REFUND", "부분 환불 승인", BigDecimal.valueOf(10000));

            given(courierClaimService.findById(100L)).willReturn(claim);

            // act
            CourierClaimResponse result =
                    courierClaimAppService.approveClaim(100L, request);

            // assert
            assertThat(result.claimStatus()).isEqualTo(CourierClaimStatus.RESOLVED);
            assertThat(result.refundAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
            verify(courierRefundService).refund(order, BigDecimal.valueOf(10000));
        }

        @Test
        @DisplayName("승인 불가 상태(APPROVED)에서 AdminValidateException 발생")
        void approveClaim_alreadyApproved_throwsException() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.DELIVERED, "C-26021400-ABCD1");
            CourierClaim claim = createClaim(order, null, CourierClaimStatus.APPROVED);
            CourierClaimApproveRequest request =
                    new CourierClaimApproveRequest("REFUND", "승인", BigDecimal.valueOf(10000));

            given(courierClaimService.findById(100L)).willReturn(claim);

            // act / assert
            assertThatThrownBy(() -> courierClaimAppService.approveClaim(100L, request))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("승인 가능한 상태가 아닙니다");
        }

        @Test
        @DisplayName("승인 불가 상태(REJECTED)에서 AdminValidateException 발생")
        void approveClaim_invalidStatus_throwsException() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.DELIVERED, "C-26021400-ABCD1");
            CourierClaim claim = createClaim(order, null, CourierClaimStatus.REJECTED);
            CourierClaimApproveRequest request =
                    new CourierClaimApproveRequest("REFUND", "승인", BigDecimal.valueOf(10000));

            given(courierClaimService.findById(100L)).willReturn(claim);

            // act / assert
            assertThatThrownBy(() -> courierClaimAppService.approveClaim(100L, request))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("승인 가능한 상태가 아닙니다");
        }
    }

    // === rejectClaim ===

    @Nested
    @DisplayName("rejectClaim - 관리자 클레임 거부")
    class RejectClaim {

        @Test
        @DisplayName("REQUESTED 상태에서 거부 성공")
        void rejectClaim_success() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.DELIVERED, "C-26021400-ABCD1");
            CourierOrderItem item = createOrderItem(order, "감귤", 2);
            CourierClaim claim = createClaim(order, item, CourierClaimStatus.REQUESTED);

            given(courierClaimService.findById(100L)).willReturn(claim);

            // act
            CourierClaimResponse result =
                    courierClaimAppService.rejectClaim(100L, "사유 부족으로 거부");

            // assert
            assertThat(result.claimStatus()).isEqualTo(CourierClaimStatus.REJECTED);
            assertThat(result.adminNote()).isEqualTo("사유 부족으로 거부");
            assertThat(claim.getResolvedAt()).isNotNull();
            assertThat(item.getItemStatus())
                    .isEqualTo(
                            store.onuljang.shared.entity.enums.CourierOrderItemStatus
                                    .CLAIM_RESOLVED);
        }

        @Test
        @DisplayName("거부 불가 상태(RESOLVED)에서 AdminValidateException 발생")
        void rejectClaim_invalidStatus_throwsException() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.DELIVERED, "C-26021400-ABCD1");
            CourierClaim claim = createClaim(order, null, CourierClaimStatus.RESOLVED);

            given(courierClaimService.findById(100L)).willReturn(claim);

            // act / assert
            assertThatThrownBy(
                            () -> courierClaimAppService.rejectClaim(100L, "거부 사유"))
                    .isInstanceOf(AdminValidateException.class)
                    .hasMessageContaining("거부 가능한 상태가 아닙니다");
        }
    }

    // === getAdminClaims ===

    @Nested
    @DisplayName("getAdminClaims - 관리자 클레임 목록 조회")
    class GetAdminClaims {

        @Test
        @DisplayName("클레임 목록을 반환한다")
        void getAdminClaims_success() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.DELIVERED, "C-26021400-ABCD1");
            CourierClaim claim1 = createClaim(order, null, CourierClaimStatus.REQUESTED);
            CourierClaim claim2 = createClaim(order, null, CourierClaimStatus.IN_REVIEW);
            ReflectionTestUtils.setField(claim2, "id", 101L);

            Page<CourierClaim> page =
                    new PageImpl<>(List.of(claim1, claim2), PageRequest.of(0, 50), 2);
            given(courierClaimService.findAllByStatus(null, 0, 50))
                    .willReturn(page);

            // act
            CourierClaimListResponse result =
                    courierClaimAppService.getAdminClaims(null, 0, 50);

            // assert
            assertThat(result.claims()).hasSize(2);
            assertThat(result.totalElements()).isEqualTo(2);
            assertThat(result.totalPages()).isEqualTo(1);
        }

        @Test
        @DisplayName("상태 필터로 클레임 목록을 반환한다")
        void getAdminClaims_withStatusFilter() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.DELIVERED, "C-26021400-ABCD1");
            CourierClaim claim = createClaim(order, null, CourierClaimStatus.REQUESTED);

            Page<CourierClaim> page =
                    new PageImpl<>(List.of(claim), PageRequest.of(0, 50), 1);
            given(courierClaimService.findAllByStatus(CourierClaimStatus.REQUESTED, 0, 50))
                    .willReturn(page);

            // act
            CourierClaimListResponse result =
                    courierClaimAppService.getAdminClaims(CourierClaimStatus.REQUESTED, 0, 50);

            // assert
            assertThat(result.claims()).hasSize(1);
            assertThat(result.claims().get(0).claimStatus())
                    .isEqualTo(CourierClaimStatus.REQUESTED);
        }
    }

    // === getAdminClaim ===

    @Nested
    @DisplayName("getAdminClaim - 관리자 클레임 상세 조회")
    class GetAdminClaim {

        @Test
        @DisplayName("단일 클레임 상세를 반환한다")
        void getAdminClaim_success() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.DELIVERED, "C-26021400-ABCD1");
            CourierOrderItem item = createOrderItem(order, "감귤", 2);
            CourierClaim claim = createClaim(order, item, CourierClaimStatus.REQUESTED);

            given(courierClaimService.findById(100L)).willReturn(claim);

            // act
            CourierClaimResponse result = courierClaimAppService.getAdminClaim(100L);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(100L);
            assertThat(result.claimType()).isEqualTo(CourierClaimType.QUALITY_ISSUE);
            assertThat(result.claimStatus()).isEqualTo(CourierClaimStatus.REQUESTED);
            assertThat(result.orderItemId()).isEqualTo(10L);
            assertThat(result.productName()).isEqualTo("감귤");
        }
    }

    // === getClaimsByOrder ===

    @Nested
    @DisplayName("getClaimsByOrder - 사용자 주문별 클레임 조회")
    class GetClaimsByOrder {

        @Test
        @DisplayName("주문에 대한 클레임 목록을 반환한다")
        void getClaimsByOrder_success() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.DELIVERED, "C-26021400-ABCD1");
            CourierClaim claim = createClaim(order, null, CourierClaimStatus.REQUESTED);

            given(userService.findByUId(testUid)).willReturn(testUser);
            given(courierOrderService.findByDisplayCodeAndUser("C-26021400-ABCD1", testUser))
                    .willReturn(order);
            given(courierClaimService.findByOrder(order)).willReturn(List.of(claim));

            // act
            List<CourierClaimResponse> result =
                    courierClaimAppService.getClaimsByOrder(testUid, "C-26021400-ABCD1");

            // assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).claimStatus()).isEqualTo(CourierClaimStatus.REQUESTED);
        }
    }
}
