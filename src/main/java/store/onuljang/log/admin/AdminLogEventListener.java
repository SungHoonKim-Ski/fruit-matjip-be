package store.onuljang.log.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import store.onuljang.repository.AdminLogRepository;
import store.onuljang.repository.entity.log.AdminLog;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminLogEventListener {
    AdminLogRepository adminLogRepository;

    @Async
    @EventListener
    public void handle(AdminLogEvent event) {
        adminLogRepository.save(AdminLog.from(event));
    }
}
