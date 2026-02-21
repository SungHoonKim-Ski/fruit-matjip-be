package store.onuljang.shared.user.event;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import store.onuljang.shared.user.repository.UserLogRepository;
import store.onuljang.shared.user.entity.log.UserLog;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserLogEventListener {
    UserLogRepository userLogRepository;

    @Async("logExecutor")
    @EventListener
    public void handle(UserLogEvent event) {
        try {
            userLogRepository.save(UserLog.from(event));
        } catch (Exception e) {
            log.warn("User log save failed", e);
        }
    }
}
