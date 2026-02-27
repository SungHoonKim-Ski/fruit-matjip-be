package store.onuljang.worker.scheduler.courier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.service.CourierOrderService;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourierTrackingRequestScheduler {

    @NonNull CourierOrderService courierOrderService;
    @NonNull SqsClient sqsClient;
    @NonNull ObjectMapper objectMapper;

    @NonFinal
    @Value("${cloud.aws.sqs.courier-tracking-request-queue-url}")
    String trackingRequestQueueUrl;

    @Retryable(
        retryFor = {
            CannotAcquireLockException.class,
            PessimisticLockingFailureException.class,
            LockAcquisitionException.class,
        },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
    )
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void run() {
        List<CourierOrder> activeOrders = courierOrderService.findActiveDeliveries();
        if (activeOrders.isEmpty()) {
            log.info("[CourierTrackingRequestScheduler] no active deliveries to track");
            return;
        }

        int sent = 0;
        List<CourierOrder> batch = new ArrayList<>();
        for (CourierOrder order : activeOrders) {
            batch.add(order);
            if (batch.size() == 10) {
                sendBatch(batch);
                sent += batch.size();
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            sendBatch(batch);
            sent += batch.size();
        }

        log.info("[CourierTrackingRequestScheduler] sent tracking requests: count={}", sent);
    }

    private void sendBatch(List<CourierOrder> orders) {
        List<SendMessageBatchRequestEntry> entries = new ArrayList<>();
        for (int i = 0; i < orders.size(); i++) {
            CourierOrder order = orders.get(i);
            try {
                String body = objectMapper.writeValueAsString(Map.of(
                    "displayCode", order.getDisplayCode(),
                    "trackingNumber", order.getWaybillNumber(),
                    "courierCompany", order.getCourierCompany().name()
                ));
                entries.add(SendMessageBatchRequestEntry.builder()
                    .id(String.valueOf(i))
                    .messageBody(body)
                    .build());
            } catch (JsonProcessingException e) {
                log.error("[CourierTrackingRequestScheduler] failed to serialize order: displayCode={}", order.getDisplayCode(), e);
            }
        }
        if (entries.isEmpty()) {
            return;
        }
        sqsClient.sendMessageBatch(SendMessageBatchRequest.builder()
            .queueUrl(trackingRequestQueueUrl)
            .entries(entries)
            .build());
    }

    @Recover
    public void recover(Exception e) {
        log.error("[CourierTrackingRequestScheduler] job failed after retries: {}", e.getMessage(), e);
    }
}
