package store.onuljang.worker.scheduler.courier;

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
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TrackingResultPollingScheduler {

    @NonNull SqsClient sqsClient;
    @NonNull TrackingResultMessageProcessor messageProcessor;

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
        if (trackingResultQueueUrl == null || trackingResultQueueUrl.isBlank()) {
            return;
        }

        var response = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
            .queueUrl(trackingResultQueueUrl)
            .maxNumberOfMessages(10)
            .waitTimeSeconds(0)
            .build());
        if (response == null) {
            return;
        }

        List<Message> messages = response.messages();
        for (Message message : messages) {
            messageProcessor.process(message, trackingResultQueueUrl);
        }
    }

    @Recover
    public void recover(Exception e) {
        log.error("[TrackingResultPollingScheduler] job failed after retries: {}", e.getMessage(), e);
    }
}
