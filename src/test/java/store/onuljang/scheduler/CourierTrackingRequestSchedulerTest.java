package store.onuljang.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResponse;
import store.onuljang.config.TestS3Config;
import store.onuljang.config.TestSqsConfig;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.scheduler.CourierTrackingRequestScheduler;
import store.onuljang.courier.service.CourierOrderService;
import store.onuljang.shared.entity.enums.CourierCompany;
import store.onuljang.shared.entity.enums.CourierOrderStatus;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestS3Config.class, TestSqsConfig.class})
class CourierTrackingRequestSchedulerTest {

    @Autowired
    CourierTrackingRequestScheduler scheduler;

    @MockitoBean
    CourierOrderService courierOrderService;

    @MockitoBean
    SqsClient sqsClient;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        when(sqsClient.sendMessageBatch(any(SendMessageBatchRequest.class)))
            .thenReturn(SendMessageBatchResponse.builder().build());
    }

    private CourierOrder buildShippedOrder(String displayCode, String waybillNumber) {
        CourierOrder order = CourierOrder.builder()
            .user(null)
            .displayCode(displayCode)
            .status(CourierOrderStatus.ORDER_COMPLETED)
            .receiverName("홍길동")
            .receiverPhone("010-1234-5678")
            .postalCode("06134")
            .address1("서울시 강남구 테헤란로 1")
            .productAmount(new BigDecimal("10000"))
            .shippingFee(new BigDecimal("3000"))
            .totalAmount(new BigDecimal("13000"))
            .build();
        order.markOrderCompleted(waybillNumber, CourierCompany.LOGEN);
        return order;
    }

    @Test
    @DisplayName("활성 배송 주문이 없으면 SQS 메시지를 전송하지 않음")
    void run_noActiveDeliveries_doesNotSendMessages() {
        // Arrange
        when(courierOrderService.findActiveDeliveries()).thenReturn(Collections.emptyList());

        // Act
        scheduler.run();

        // Assert
        verify(sqsClient, never()).sendMessageBatch(any(SendMessageBatchRequest.class));
    }

    @Test
    @DisplayName("활성 배송 주문이 있으면 SQS에 메시지를 전송함")
    void run_withActiveDeliveries_sendsBatchMessages() {
        // Arrange
        CourierOrder order1 = buildShippedOrder("C-001", "1234567890");
        CourierOrder order2 = buildShippedOrder("C-002", "0987654321");
        when(courierOrderService.findActiveDeliveries()).thenReturn(List.of(order1, order2));

        // Act
        scheduler.run();

        // Assert
        verify(sqsClient, times(1)).sendMessageBatch(any(SendMessageBatchRequest.class));
    }

    @Test
    @DisplayName("10개 초과 주문은 여러 배치로 나누어 전송됨")
    void run_moreThanTenOrders_sendsMultipleBatches() {
        // Arrange
        List<CourierOrder> orders = new java.util.ArrayList<>();
        for (int i = 0; i < 15; i++) {
            orders.add(buildShippedOrder("C-" + i, "WB-" + i));
        }
        when(courierOrderService.findActiveDeliveries()).thenReturn(orders);

        // Act
        scheduler.run();

        // Assert: 15개 = 배치 2번 (10 + 5)
        verify(sqsClient, times(2)).sendMessageBatch(any(SendMessageBatchRequest.class));
    }

    @Test
    @DisplayName("전송 메시지에 displayCode, trackingNumber, courierCompany가 포함됨")
    void run_withActiveDelivery_messageContainsRequiredFields() throws Exception {
        // Arrange
        CourierOrder order = buildShippedOrder("C-ABC", "WB-XYZ");
        when(courierOrderService.findActiveDeliveries()).thenReturn(List.of(order));

        ArgumentCaptor<SendMessageBatchRequest> captor = ArgumentCaptor.forClass(SendMessageBatchRequest.class);

        // Act
        scheduler.run();

        // Assert
        verify(sqsClient).sendMessageBatch(captor.capture());
        SendMessageBatchRequest captured = captor.getValue();
        assertThat(captured.entries()).hasSize(1);
        String body = captured.entries().get(0).messageBody();
        assertThat(body).contains("C-ABC");
        assertThat(body).contains("WB-XYZ");
        assertThat(body).contains("LOGEN");
    }
}
