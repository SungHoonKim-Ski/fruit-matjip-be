package store.onuljang.log.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import store.onuljang.repository.AdminLogRepository;
import store.onuljang.repository.entity.log.AdminLog;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminLogEventListener {
    AdminLogRepository adminLogRepository;

    @Async("logExecutor")
    @EventListener
    public void handle(AdminLogEvent event) {
        try {
            adminLogRepository.save(AdminLog.from(event));
        } catch (Exception e) {
            log.warn("Admin log save failed", e);
        }
    }
}
