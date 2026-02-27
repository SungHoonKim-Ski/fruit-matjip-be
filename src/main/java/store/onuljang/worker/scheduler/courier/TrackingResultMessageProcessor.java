package store.onuljang.worker.scheduler.courier;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.event.CourierStatusChangedEvent;
import store.onuljang.courier.service.CourierOrderService;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.util.TimeUtil;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TrackingResultMessageProcessor {

    @NonNull CourierOrderService courierOrderService;
    @NonNull SqsClient sqsClient;
    @NonNull ObjectMapper objectMapper;
    @NonNull ApplicationEventPublisher eventPublisher;

    @Transactional
    public void process(Message message, String queueUrl) {
        try {
            TrackingResultMessage result = objectMapper.readValue(
                message.body(), TrackingResultMessage.class);

            CourierOrder order = courierOrderService.findByDisplayCode(result.displayCode());
            CourierOrderStatus current = order.getStatus();
            String newStatus = result.status();

            if ("IN_TRANSIT".equals(newStatus) && current == CourierOrderStatus.ORDER_COMPLETED) {
                order.markInTransit();
                log.info("[TrackingResultMessageProcessor] status updated: displayCode={}, {} -> IN_TRANSIT",
                    result.displayCode(), current);
                publishStatusEvent(order, CourierOrderStatus.IN_TRANSIT);
            } else if ("DELIVERED".equals(newStatus)
                && (current == CourierOrderStatus.ORDER_COMPLETED || current == CourierOrderStatus.IN_TRANSIT)) {
                order.markDelivered();
                log.info("[TrackingResultMessageProcessor] status updated: displayCode={}, {} -> DELIVERED",
                    result.displayCode(), current);
                publishStatusEvent(order, CourierOrderStatus.DELIVERED);
            }

            if (result.location() != null && !result.location().isBlank()) {
                LocalDateTime trackingTime = parseTrackingTimestamp(result.timestamp());
                order.updateTrackingInfo(result.location(), trackingTime);
            }

            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build());
        } catch (Exception e) {
            log.error("[TrackingResultMessageProcessor] failed to process message: messageId={}, error={}",
                message.messageId(), e.getMessage(), e);
        }
    }

    private void publishStatusEvent(CourierOrder order, CourierOrderStatus newStatus) {
        eventPublisher.publishEvent(new CourierStatusChangedEvent(
            order.getDisplayCode(),
            newStatus,
            order.getReceiverPhone(),
            order.getReceiverName(),
            order.getCourierCompany() != null ? order.getCourierCompany().name() : null,
            order.getWaybillNumber(),
            order.getProductSummary()
        ));
    }

    private LocalDateTime parseTrackingTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return TimeUtil.nowDateTime();
        }
        try {
            return LocalDateTime.parse(timestamp, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return TimeUtil.nowDateTime();
        }
    }
}
