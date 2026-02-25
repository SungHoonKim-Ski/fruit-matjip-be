package store.onuljang.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import store.onuljang.config.TestS3Config;
import store.onuljang.config.TestSqsConfig;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.scheduler.TrackingResultMessageProcessor;
import store.onuljang.courier.scheduler.TrackingResultPollingScheduler;
import store.onuljang.courier.service.CourierOrderService;
import store.onuljang.shared.entity.enums.CourierCompany;
import store.onuljang.shared.entity.enums.CourierOrderStatus;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestS3Config.class, TestSqsConfig.class})
class TrackingResultPollingSchedulerTest {

    @Autowired
    TrackingResultPollingScheduler scheduler;

    @Autowired
    TrackingResultMessageProcessor messageProcessor;

    @MockitoBean
    SqsClient sqsClient;

    @MockitoBean
    CourierOrderService courierOrderService;

    @Autowired
    ObjectMapper objectMapper;

    private CourierOrder buildOrder(String displayCode, CourierOrderStatus status) {
        CourierOrder order = CourierOrder.builder()
            .user(null)
            .displayCode(displayCode)
            .status(status)
            .receiverName("홍길동")
            .receiverPhone("010-1234-5678")
            .postalCode("06134")
            .address1("서울시 강남구 테헤란로 1")
            .productAmount(new BigDecimal("10000"))
            .shippingFee(new BigDecimal("3000"))
            .totalAmount(new BigDecimal("13000"))
            .build();
        if (status == CourierOrderStatus.ORDER_COMPLETED || status == CourierOrderStatus.IN_TRANSIT) {
            org.springframework.test.util.ReflectionTestUtils.setField(order, "waybillNumber", "WB-001");
            org.springframework.test.util.ReflectionTestUtils.setField(order, "courierCompany", CourierCompany.LOGEN);
        }
        return order;
    }

    private Message buildMessage(String displayCode, String status) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "displayCode", displayCode,
            "status", status,
            "location", "서울 강남",
            "timestamp", "2026-02-24T10:00:00"
        ));
        return Message.builder()
            .messageId("msg-" + displayCode)
            .receiptHandle("receipt-" + displayCode)
            .body(body)
            .build();
    }

    @Test
    @DisplayName("큐에 메시지가 없으면 아무 처리도 하지 않음")
    void run_noMessages_doesNothing() {
        // Arrange
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
            .thenReturn(ReceiveMessageResponse.builder().messages(Collections.emptyList()).build());

        // Act
        scheduler.run();

        // Assert
        verify(courierOrderService, never()).findByDisplayCode(any());
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("ORDER_COMPLETED 주문에 IN_TRANSIT 결과 수신 시 markInTransit 호출 및 메시지 삭제")
    void processMessage_shippedOrder_inTransitResult_marksInTransit() throws Exception {
        // Arrange
        CourierOrder order = buildOrder("C-001", CourierOrderStatus.ORDER_COMPLETED);
        when(courierOrderService.findByDisplayCode("C-001")).thenReturn(order);

        Message message = buildMessage("C-001", "IN_TRANSIT");

        // Act
        messageProcessor.process(message, "test-queue-url");

        // Assert
        assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.IN_TRANSIT);
        ArgumentCaptor<DeleteMessageRequest> captor = ArgumentCaptor.forClass(DeleteMessageRequest.class);
        verify(sqsClient).deleteMessage(captor.capture());
        assertThat(captor.getValue().receiptHandle()).isEqualTo("receipt-C-001");
    }

    @Test
    @DisplayName("ORDER_COMPLETED 주문에 DELIVERED 결과 수신 시 markDelivered 호출 및 메시지 삭제")
    void processMessage_shippedOrder_deliveredResult_marksDelivered() throws Exception {
        // Arrange
        CourierOrder order = buildOrder("C-002", CourierOrderStatus.ORDER_COMPLETED);
        when(courierOrderService.findByDisplayCode("C-002")).thenReturn(order);

        Message message = buildMessage("C-002", "DELIVERED");

        // Act
        messageProcessor.process(message, "test-queue-url");

        // Assert
        assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.DELIVERED);
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("IN_TRANSIT 주문에 DELIVERED 결과 수신 시 markDelivered 호출")
    void processMessage_inTransitOrder_deliveredResult_marksDelivered() throws Exception {
        // Arrange
        CourierOrder order = buildOrder("C-003", CourierOrderStatus.IN_TRANSIT);
        when(courierOrderService.findByDisplayCode("C-003")).thenReturn(order);

        Message message = buildMessage("C-003", "DELIVERED");

        // Act
        messageProcessor.process(message, "test-queue-url");

        // Assert
        assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.DELIVERED);
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("이미 DELIVERED 상태인 주문에 IN_TRANSIT 결과 수신 시 상태 변경 없음 (역방향 전환 방지)")
    void processMessage_deliveredOrder_inTransitResult_noStatusChange() throws Exception {
        // Arrange
        CourierOrder order = buildOrder("C-004", CourierOrderStatus.DELIVERED);
        when(courierOrderService.findByDisplayCode("C-004")).thenReturn(order);

        Message message = buildMessage("C-004", "IN_TRANSIT");

        // Act
        messageProcessor.process(message, "test-queue-url");

        // Assert
        assertThat(order.getStatus()).isEqualTo(CourierOrderStatus.DELIVERED);
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("큐에서 여러 메시지 수신 시 각각 처리함")
    void run_multipleMessages_processesAll() throws Exception {
        // Arrange
        CourierOrder order1 = buildOrder("C-010", CourierOrderStatus.ORDER_COMPLETED);
        CourierOrder order2 = buildOrder("C-011", CourierOrderStatus.ORDER_COMPLETED);
        when(courierOrderService.findByDisplayCode("C-010")).thenReturn(order1);
        when(courierOrderService.findByDisplayCode("C-011")).thenReturn(order2);

        Message msg1 = buildMessage("C-010", "IN_TRANSIT");
        Message msg2 = buildMessage("C-011", "DELIVERED");

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
            .thenReturn(ReceiveMessageResponse.builder().messages(List.of(msg1, msg2)).build());

        // Act
        scheduler.run();

        // Assert
        assertThat(order1.getStatus()).isEqualTo(CourierOrderStatus.IN_TRANSIT);
        assertThat(order2.getStatus()).isEqualTo(CourierOrderStatus.DELIVERED);
    }
}
