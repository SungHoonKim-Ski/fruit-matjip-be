package store.onuljang.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import store.onuljang.repository.entity.DeliveryOrder;
import store.onuljang.repository.entity.enums.DeliveryStatus;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryOrderTest {

    private DeliveryOrder createOrder(DeliveryStatus status) {
        return DeliveryOrder.builder()
            .status(status)
            .build();
    }

    // --- canMarkPaid ---

    @Test
    @DisplayName("PENDING_PAYMENT 상태에서 결제 가능")
    void canMarkPaid_pendingPayment_returnsTrue() {
        // given
        DeliveryOrder order = createOrder(DeliveryStatus.PENDING_PAYMENT);

        // when
        boolean result = order.canMarkPaid();

        // then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = DeliveryStatus.class, names = {"PAID", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELED", "FAILED"})
    @DisplayName("PENDING_PAYMENT 외 상태에서 결제 불가")
    void canMarkPaid_otherStatuses_returnsFalse(DeliveryStatus status) {
        // given
        DeliveryOrder order = createOrder(status);

        // when
        boolean result = order.canMarkPaid();

        // then
        assertThat(result).isFalse();
    }

    // --- canMarkOutForDelivery ---

    @Test
    @DisplayName("PAID 상태에서 배달 시작 가능")
    void canMarkOutForDelivery_paid_returnsTrue() {
        // given
        DeliveryOrder order = createOrder(DeliveryStatus.PAID);

        // when / then
        assertThat(order.canMarkOutForDelivery()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = DeliveryStatus.class, names = {"PENDING_PAYMENT", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELED", "FAILED"})
    @DisplayName("PAID 외 상태에서 배달 시작 불가")
    void canMarkOutForDelivery_otherStatuses_returnsFalse(DeliveryStatus status) {
        // given
        DeliveryOrder order = createOrder(status);

        // when / then
        assertThat(order.canMarkOutForDelivery()).isFalse();
    }

    // --- canMarkDelivered ---

    @Test
    @DisplayName("OUT_FOR_DELIVERY 상태에서 배달 완료 가능")
    void canMarkDelivered_outForDelivery_returnsTrue() {
        // given
        DeliveryOrder order = createOrder(DeliveryStatus.OUT_FOR_DELIVERY);

        // when / then
        assertThat(order.canMarkDelivered()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = DeliveryStatus.class, names = {"PENDING_PAYMENT", "PAID", "DELIVERED", "CANCELED", "FAILED"})
    @DisplayName("OUT_FOR_DELIVERY 외 상태에서 배달 완료 불가")
    void canMarkDelivered_otherStatuses_returnsFalse(DeliveryStatus status) {
        // given
        DeliveryOrder order = createOrder(status);

        // when / then
        assertThat(order.canMarkDelivered()).isFalse();
    }

    // --- canCancelByAdmin ---

    @ParameterizedTest
    @EnumSource(value = DeliveryStatus.class, names = {"PENDING_PAYMENT", "PAID", "OUT_FOR_DELIVERY"})
    @DisplayName("관리자 취소 가능 상태")
    void canCancelByAdmin_activeStatuses_returnsTrue(DeliveryStatus status) {
        // given
        DeliveryOrder order = createOrder(status);

        // when / then
        assertThat(order.canCancelByAdmin()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = DeliveryStatus.class, names = {"DELIVERED", "CANCELED", "FAILED"})
    @DisplayName("관리자 취소 불가 상태")
    void canCancelByAdmin_terminalStatuses_returnsFalse(DeliveryStatus status) {
        // given
        DeliveryOrder order = createOrder(status);

        // when / then
        assertThat(order.canCancelByAdmin()).isFalse();
    }

    // --- canCancelByUser ---

    @Test
    @DisplayName("사용자 취소는 PENDING_PAYMENT만 가능")
    void canCancelByUser_pendingPayment_returnsTrue() {
        // given
        DeliveryOrder order = createOrder(DeliveryStatus.PENDING_PAYMENT);

        // when / then
        assertThat(order.canCancelByUser()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = DeliveryStatus.class, names = {"PAID", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELED", "FAILED"})
    @DisplayName("사용자 취소는 PENDING_PAYMENT 외 불가")
    void canCancelByUser_otherStatuses_returnsFalse(DeliveryStatus status) {
        // given
        DeliveryOrder order = createOrder(status);

        // when / then
        assertThat(order.canCancelByUser()).isFalse();
    }

    // --- canFailByUser ---

    @Test
    @DisplayName("사용자 실패 처리는 PENDING_PAYMENT만 가능")
    void canFailByUser_pendingPayment_returnsTrue() {
        // given
        DeliveryOrder order = createOrder(DeliveryStatus.PENDING_PAYMENT);

        // when / then
        assertThat(order.canFailByUser()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = DeliveryStatus.class, names = {"PAID", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELED", "FAILED"})
    @DisplayName("사용자 실패 처리는 PENDING_PAYMENT 외 불가")
    void canFailByUser_otherStatuses_returnsFalse(DeliveryStatus status) {
        // given
        DeliveryOrder order = createOrder(status);

        // when / then
        assertThat(order.canFailByUser()).isFalse();
    }

    // --- isPaid ---

    @ParameterizedTest
    @EnumSource(value = DeliveryStatus.class, names = {"PAID", "OUT_FOR_DELIVERY"})
    @DisplayName("PAID, OUT_FOR_DELIVERY는 결제 완료 상태")
    void isPaid_paidOrOutForDelivery_returnsTrue(DeliveryStatus status) {
        // given
        DeliveryOrder order = createOrder(status);

        // when / then
        assertThat(order.isPaid()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = DeliveryStatus.class, names = {"PENDING_PAYMENT", "DELIVERED", "CANCELED", "FAILED"})
    @DisplayName("PAID, OUT_FOR_DELIVERY 외는 결제 미완료 상태")
    void isPaid_otherStatuses_returnsFalse(DeliveryStatus status) {
        // given
        DeliveryOrder order = createOrder(status);

        // when / then
        assertThat(order.isPaid()).isFalse();
    }

    // --- status transitions ---

    @Test
    @DisplayName("markPaid는 PAID로 변경하고 paidAt 설정")
    void markPaid_changesStatusAndSetsPaidAt() {
        // given
        DeliveryOrder order = createOrder(DeliveryStatus.PENDING_PAYMENT);

        // when
        order.markPaid();

        // then
        assertThat(order.getStatus()).isEqualTo(DeliveryStatus.PAID);
        assertThat(order.getPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("markOutForDelivery는 OUT_FOR_DELIVERY로 변경")
    void markOutForDelivery_changesStatus() {
        // given
        DeliveryOrder order = createOrder(DeliveryStatus.PAID);

        // when
        order.markOutForDelivery();

        // then
        assertThat(order.getStatus()).isEqualTo(DeliveryStatus.OUT_FOR_DELIVERY);
    }

    @Test
    @DisplayName("markDelivered는 DELIVERED로 변경")
    void markDelivered_changesStatus() {
        // given
        DeliveryOrder order = createOrder(DeliveryStatus.OUT_FOR_DELIVERY);

        // when
        order.markDelivered();

        // then
        assertThat(order.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    @DisplayName("markFailed는 FAILED로 변경")
    void markFailed_changesStatus() {
        // given
        DeliveryOrder order = createOrder(DeliveryStatus.PENDING_PAYMENT);

        // when
        order.markFailed();

        // then
        assertThat(order.getStatus()).isEqualTo(DeliveryStatus.FAILED);
    }

    @Test
    @DisplayName("markCanceled는 CANCELED로 변경")
    void markCanceled_changesStatus() {
        // given
        DeliveryOrder order = createOrder(DeliveryStatus.PENDING_PAYMENT);

        // when
        order.markCanceled();

        // then
        assertThat(order.getStatus()).isEqualTo(DeliveryStatus.CANCELED);
    }
}
