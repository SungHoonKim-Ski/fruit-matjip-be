package store.onuljang.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.repository.CourierOrderRepository;
import store.onuljang.courier.service.CourierOrderService;
import store.onuljang.shared.entity.enums.CourierCompany;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.user.entity.Users;
import org.springframework.context.annotation.Import;
import store.onuljang.config.TestSqsConfig;
import store.onuljang.support.IntegrationTestBase;

@Import(TestSqsConfig.class)
@DisplayName("CourierOrderService.findActiveDeliveries 통합 테스트")
class CourierActiveDeliveriesIntegrationTest extends IntegrationTestBase {

    @Autowired
    CourierOrderRepository courierOrderRepository;

    @Autowired
    CourierOrderService courierOrderService;

    private Users user;

    @BeforeEach
    void setUp() {
        user = testFixture.createUser("택배고객");
    }

    private CourierOrder saveOrder(CourierOrderStatus status, String waybillNumber) {
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
        if (waybillNumber != null) {
            org.springframework.test.util.ReflectionTestUtils.setField(order, "waybillNumber", waybillNumber);
            org.springframework.test.util.ReflectionTestUtils.setField(order, "courierCompany", CourierCompany.LOGEN);
        }
        return courierOrderRepository.save(order);
    }

    @Test
    @DisplayName("SHIPPED 상태이고 운송장번호가 있는 주문을 반환함")
    void findActiveDeliveries_shippedWithWaybill_returned() {
        // Arrange
        saveOrder(CourierOrderStatus.SHIPPED, "WB-001");

        // Act
        List<CourierOrder> result = courierOrderService.findActiveDeliveries();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(CourierOrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("IN_TRANSIT 상태이고 운송장번호가 있는 주문을 반환함")
    void findActiveDeliveries_inTransitWithWaybill_returned() {
        // Arrange
        saveOrder(CourierOrderStatus.IN_TRANSIT, "WB-002");

        // Act
        List<CourierOrder> result = courierOrderService.findActiveDeliveries();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(CourierOrderStatus.IN_TRANSIT);
    }

    @Test
    @DisplayName("운송장번호가 없는 SHIPPED 주문은 반환하지 않음")
    void findActiveDeliveries_shippedWithoutWaybill_notReturned() {
        // Arrange
        saveOrder(CourierOrderStatus.SHIPPED, null);

        // Act
        List<CourierOrder> result = courierOrderService.findActiveDeliveries();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("DELIVERED, PAID 등 비활성 상태 주문은 반환하지 않음")
    void findActiveDeliveries_nonActiveStatuses_notReturned() {
        // Arrange
        saveOrder(CourierOrderStatus.DELIVERED, "WB-003");
        saveOrder(CourierOrderStatus.PAID, "WB-004");
        saveOrder(CourierOrderStatus.CANCELED, "WB-005");

        // Act
        List<CourierOrder> result = courierOrderService.findActiveDeliveries();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SHIPPED와 IN_TRANSIT 혼합 주문 모두 반환함")
    void findActiveDeliveries_mixedActiveStatuses_allReturned() {
        // Arrange
        saveOrder(CourierOrderStatus.SHIPPED, "WB-010");
        saveOrder(CourierOrderStatus.IN_TRANSIT, "WB-011");
        saveOrder(CourierOrderStatus.DELIVERED, "WB-012");

        // Act
        List<CourierOrder> result = courierOrderService.findActiveDeliveries();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(CourierOrder::getStatus)
            .containsExactlyInAnyOrder(CourierOrderStatus.SHIPPED, CourierOrderStatus.IN_TRANSIT);
    }

    @Test
    @DisplayName("활성 배송 주문이 없으면 빈 목록을 반환함")
    void findActiveDeliveries_noOrders_returnsEmpty() {
        // Act
        List<CourierOrder> result = courierOrderService.findActiveDeliveries();

        // Assert
        assertThat(result).isEmpty();
    }
}
