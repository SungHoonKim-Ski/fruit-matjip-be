package store.onuljang.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.util.ReflectionTestUtils;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierOrderItem;
import store.onuljang.shared.entity.enums.CourierOrderStatus;

class CourierOrderTest {

    private CourierOrder createOrder(CourierOrderStatus status) {
        return CourierOrder.builder()
                .status(status)
                .displayCode("C-26021400-ABC12")
                .receiverName("홍길동")
                .receiverPhone("010-1234-5678")
                .postalCode("06134")
                .address1("서울시 강남구")
                .productAmount(BigDecimal.valueOf(30000))
                .shippingFee(BigDecimal.valueOf(4000))
                .totalAmount(BigDecimal.valueOf(34000))
                .build();
    }

    private CourierOrderItem createItem(CourierOrder order, String productName, int quantity) {
        return CourierOrderItem.builder()
                .courierOrder(order)
                .productName(productName)
                .productPrice(BigDecimal.valueOf(10000))
                .quantity(quantity)
                .amount(BigDecimal.valueOf(10000L * quantity))
                .build();
    }

    // --- canMarkPaid ---

    @Nested
    @DisplayName("canMarkPaid - 결제 가능 여부")
    class CanMarkPaid {

        @Test
        @DisplayName("PENDING_PAYMENT 상태에서 결제 가능")
        void canMarkPaid_pendingPayment_returnsTrue() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PENDING_PAYMENT);

            // act
            boolean result = order.canMarkPaid();

            // assert
            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @EnumSource(
                value = CourierOrderStatus.class,
                names = {"PAID", "PREPARING", "SHIPPED", "IN_TRANSIT", "DELIVERED", "CANCELED", "FAILED"})
        @DisplayName("PENDING_PAYMENT 외 상태에서 결제 불가")
        void canMarkPaid_otherStatuses_returnsFalse(CourierOrderStatus status) {
            // arrange
            CourierOrder order = createOrder(status);

            // act
            boolean result = order.canMarkPaid();

            // assert
            assertThat(result).isFalse();
        }
    }

    // --- canCancelByUser ---

    @Nested
    @DisplayName("canCancelByUser - 사용자 취소 가능 여부")
    class CanCancelByUser {

        @Test
        @DisplayName("PENDING_PAYMENT 상태에서 사용자 취소 가능")
        void canCancelByUser_pendingPayment_returnsTrue() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PENDING_PAYMENT);

            // act / assert
            assertThat(order.canCancelByUser()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(
                value = CourierOrderStatus.class,
                names = {"PAID", "PREPARING", "SHIPPED", "IN_TRANSIT", "DELIVERED", "CANCELED", "FAILED"})
        @DisplayName("PENDING_PAYMENT 외 상태에서 사용자 취소 불가")
        void canCancelByUser_otherStatuses_returnsFalse(CourierOrderStatus status) {
            // arrange
            CourierOrder order = createOrder(status);

            // act / assert
            assertThat(order.canCancelByUser()).isFalse();
        }
    }

    // --- canFailByUser ---

    @Nested
    @DisplayName("canFailByUser - 사용자 실패 처리 가능 여부")
    class CanFailByUser {

        @Test
        @DisplayName("PENDING_PAYMENT 상태에서 사용자 실패 처리 가능")
        void canFailByUser_pendingPayment_returnsTrue() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PENDING_PAYMENT);

            // act / assert
            assertThat(order.canFailByUser()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(
                value = CourierOrderStatus.class,
                names = {"PAID", "PREPARING", "SHIPPED", "IN_TRANSIT", "DELIVERED", "CANCELED", "FAILED"})
        @DisplayName("PENDING_PAYMENT 외 상태에서 사용자 실패 처리 불가")
        void canFailByUser_otherStatuses_returnsFalse(CourierOrderStatus status) {
            // arrange
            CourierOrder order = createOrder(status);

            // act / assert
            assertThat(order.canFailByUser()).isFalse();
        }
    }

    // --- status transitions ---

    @Nested
    @DisplayName("상태 전이 메서드")
    class StatusTransitions {

        @Test
        @DisplayName("markPaid는 PAID로 변경하고 paidAt, pgTid 설정")
        void markPaid_changesStatusAndSetsPaidAtAndPgTid() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PENDING_PAYMENT);

            // act
            order.markPaid("pg_tid_12345");

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.PAID);
            assertThat(order.getPaidAt()).isNotNull();
            assertThat(order.getPgTid()).isEqualTo("pg_tid_12345");
        }

        @Test
        @DisplayName("markPreparing은 PREPARING으로 변경")
        void markPreparing_changesStatus() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID);

            // act
            order.markPreparing();

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.PREPARING);
        }

        @Test
        @DisplayName("markShipped는 SHIPPED로 변경하고 waybillNumber, shippedAt 설정")
        void markShipped_changesStatusAndSetsWaybillAndShippedAt() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PREPARING);

            // act
            order.markShipped("WAYBILL-001");

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.SHIPPED);
            assertThat(order.getWaybillNumber()).isEqualTo("WAYBILL-001");
            assertThat(order.getShippedAt()).isNotNull();
        }

        @Test
        @DisplayName("markDelivered는 DELIVERED로 변경하고 deliveredAt 설정")
        void markDelivered_changesStatusAndSetsDeliveredAt() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.SHIPPED);

            // act
            order.markDelivered();

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.DELIVERED);
            assertThat(order.getDeliveredAt()).isNotNull();
        }

        @Test
        @DisplayName("markCanceled는 CANCELED로 변경")
        void markCanceled_changesStatus() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PENDING_PAYMENT);

            // act
            order.markCanceled();

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.CANCELED);
        }

        @Test
        @DisplayName("markFailed는 FAILED로 변경")
        void markFailed_changesStatus() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PENDING_PAYMENT);

            // act
            order.markFailed();

            // assert
            assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.FAILED);
        }
    }

    // --- getProductSummary ---

    @Nested
    @DisplayName("getProductSummary - 상품 요약")
    class GetProductSummary {

        @Test
        @DisplayName("상품 1건이면 상품명만 반환")
        void getProductSummary_singleItem_returnsName() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID);
            order.getItems().add(createItem(order, "제주 감귤 5kg", 1));

            // act
            String summary = order.getProductSummary();

            // assert
            assertThat(summary).isEqualTo("제주 감귤 5kg");
        }

        @Test
        @DisplayName("상품 2건 이상이면 '상품A 외 N건' 형식")
        void getProductSummary_multipleItems_returnsNameWithCount() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID);
            order.getItems().add(createItem(order, "제주 감귤 5kg", 1));
            order.getItems().add(createItem(order, "성주 참외 3kg", 2));
            order.getItems().add(createItem(order, "나주 배 7.5kg", 1));

            // act
            String summary = order.getProductSummary();

            // assert
            assertThat(summary).isEqualTo("제주 감귤 5kg 외 2건");
        }

        @Test
        @DisplayName("상품이 없으면 빈 문자열 반환")
        void getProductSummary_emptyItems_returnsEmpty() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID);

            // act
            String summary = order.getProductSummary();

            // assert
            assertThat(summary).isEmpty();
        }

        @Test
        @DisplayName("items가 null이면 빈 문자열 반환")
        void getProductSummary_nullItems_returnsEmpty() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID);
            ReflectionTestUtils.setField(order, "items", null);

            // act
            String summary = order.getProductSummary();

            // assert
            assertThat(summary).isEmpty();
        }
    }

    // --- getTotalQuantity ---

    @Nested
    @DisplayName("getTotalQuantity - 총 수량 합산")
    class GetTotalQuantity {

        @Test
        @DisplayName("items 수량 합산")
        void getTotalQuantity_multipleItems_returnsSumOfQuantities() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID);
            order.getItems().add(createItem(order, "감귤", 3));
            order.getItems().add(createItem(order, "참외", 2));

            // act
            int totalQuantity = order.getTotalQuantity();

            // assert
            assertThat(totalQuantity).isEqualTo(5);
        }

        @Test
        @DisplayName("빈 목록이면 0 반환")
        void getTotalQuantity_emptyItems_returnsZero() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID);

            // act
            int totalQuantity = order.getTotalQuantity();

            // assert
            assertThat(totalQuantity).isZero();
        }

        @Test
        @DisplayName("items가 null이면 0 반환")
        void getTotalQuantity_nullItems_returnsZero() {
            // arrange
            CourierOrder order = createOrder(CourierOrderStatus.PAID);
            ReflectionTestUtils.setField(order, "items", null);

            // act
            int totalQuantity = order.getTotalQuantity();

            // assert
            assertThat(totalQuantity).isZero();
        }
    }
}
