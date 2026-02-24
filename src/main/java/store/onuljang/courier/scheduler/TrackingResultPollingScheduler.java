package store.onuljang.courier.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.service.CourierOrderService;
import store.onuljang.shared.entity.enums.CourierOrderStatus;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TrackingResultPollingScheduler {

    @NonNull CourierOrderService courierOrderService;
    @NonNull SqsClient sqsClient;
    @NonNull ObjectMapper objectMapper;

    @NonFinal
    @Value("${cloud.aws.sqs.courier-tracking-result-queue-url}")
    String trackingResultQueueUrl;

    @Retryable(
        retryFor = {
            CannotAcquireLockException.class,
            PessimisticLockingFailureException.class,
            LockAcquisitionException.class,
        },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
    )
    @Scheduled(fixedDelay = 60_000)
    public void run() {
        List<Message> messages = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
            .queueUrl(trackingResultQueueUrl)
            .maxNumberOfMessages(10)
            .waitTimeSeconds(0)
            .build()).messages();

        for (Message message : messages) {
            processMessage(message);
        }
    }

    @Transactional
    public void processMessage(Message message) {
        try {
            TrackingResultMessage result = objectMapper.readValue(
                message.body(), TrackingResultMessage.class);

            CourierOrder order = courierOrderService.findByDisplayCode(result.displayCode());
            CourierOrderStatus current = order.getStatus();
            String newStatus = result.status();

            if ("IN_TRANSIT".equals(newStatus) && current == CourierOrderStatus.SHIPPED) {
                order.markInTransit();
                log.info("[TrackingResultPollingScheduler] status updated: displayCode={}, {} -> IN_TRANSIT",
                    result.displayCode(), current);
            } else if ("DELIVERED".equals(newStatus)
                && (current == CourierOrderStatus.SHIPPED || current == CourierOrderStatus.IN_TRANSIT)) {
                order.markDelivered();
                log.info("[TrackingResultPollingScheduler] status updated: displayCode={}, {} -> DELIVERED",
                    result.displayCode(), current);
            }

            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(trackingResultQueueUrl)
                .receiptHandle(message.receiptHandle())
                .build());
        } catch (Exception e) {
            log.error("[TrackingResultPollingScheduler] failed to process message: messageId={}, error={}",
                message.messageId(), e.getMessage(), e);
        }
    }

    @Recover
    public void recover(Exception e) {
        log.error("[TrackingResultPollingScheduler] job failed after retries: {}", e.getMessage(), e);
    }
}
