package store.onuljang.log.user_message;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store.onuljang.log.user_product.UserReservationLogEvent;
import store.onuljang.repository.UserReservationLogRepository;
import store.onuljang.repository.entity.MessageTemplate;
import store.onuljang.repository.entity.UserMessageQueue;
import store.onuljang.service.MessageTemplateService;
import store.onuljang.service.UserMessageQueueService;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserMessageEventListener {
    MessageTemplateService messageTemplateService;
    UserMessageQueueService userMessageQueueService;

    @Async("messageEventExecutor")
    @Retryable(
        retryFor = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 500)
    )
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(UserMessageEvent event) {
        MessageTemplate template = messageTemplateService.findByMessageType(event.type());

        UserMessageQueue message = UserMessageQueue.builder()
            .userUid(event.userUid())
            .template(template)
            .build();

        userMessageQueueService.save(message);
    }
}
