package store.onuljang.log.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import store.onuljang.repository.UserLogRepository;
import store.onuljang.repository.entity.log.UserLog;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserLogEventListener {
    UserLogRepository userLogRepository;

    @Async
    @EventListener
    public void handle(UserLogEvent event) {
        userLogRepository.save(UserLog.from(event));
    }
}
