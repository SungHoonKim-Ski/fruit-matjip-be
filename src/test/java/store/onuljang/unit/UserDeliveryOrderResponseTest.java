package store.onuljang.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import store.onuljang.shared.user.dto.UserDeliveryOrderResponse;
import store.onuljang.shop.delivery.entity.DeliveryOrder;
import store.onuljang.shop.reservation.entity.Reservation;
import store.onuljang.shared.entity.enums.DeliveryStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserDeliveryOrderResponseTest {

    @ParameterizedTest
    @EnumSource(value = DeliveryStatus.class, names = {"PENDING_PAYMENT", "CANCELED", "FAILED"})
    @DisplayName("PENDING_PAYMENT, CANCELED, FAILED 상태는 null 반환")
    void from_filteredStatuses_returnsNull(DeliveryStatus status) {
        // Arrange
        Reservation reservation = mock(Reservation.class);
        DeliveryOrder order = mock(DeliveryOrder.class);
        when(reservation.getDeliveryOrder()).thenReturn(order);
        when(order.getStatus()).thenReturn(status);

        // Act
        UserDeliveryOrderResponse result = UserDeliveryOrderResponse.from(reservation);

        // Assert
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = DeliveryStatus.class, names = {"PAID", "OUT_FOR_DELIVERY", "DELIVERED"})
    @DisplayName("PAID, OUT_FOR_DELIVERY, DELIVERED 상태는 응답 반환")
    void from_activeStatuses_returnsResponse(DeliveryStatus status) {
        // Arrange
        Reservation reservation = mock(Reservation.class);
        DeliveryOrder order = mock(DeliveryOrder.class);
        when(reservation.getDeliveryOrder()).thenReturn(order);
        when(reservation.getId()).thenReturn(1L);
        when(order.getStatus()).thenReturn(status);
        when(order.getDisplayCode()).thenReturn("D-1");
        when(order.getDeliveryFee()).thenReturn(java.math.BigDecimal.valueOf(2900));
        when(order.getDeliveryHour()).thenReturn(12);
        when(order.getDeliveryMinute()).thenReturn(0);

        // Act
        UserDeliveryOrderResponse result = UserDeliveryOrderResponse.from(reservation);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(status);
        assertThat(result.displayCode()).isEqualTo("D-1");
    }

    @DisplayName("deliveryOrder가 null이면 null 반환")
    @org.junit.jupiter.api.Test
    void from_noDeliveryOrder_returnsNull() {
        // Arrange
        Reservation reservation = mock(Reservation.class);
        when(reservation.getDeliveryOrder()).thenReturn(null);

        // Act
        UserDeliveryOrderResponse result = UserDeliveryOrderResponse.from(reservation);

        // Assert
        assertThat(result).isNull();
    }
}
