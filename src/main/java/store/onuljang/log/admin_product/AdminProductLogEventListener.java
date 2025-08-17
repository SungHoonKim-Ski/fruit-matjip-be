package store.onuljang.log.admin_product;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import store.onuljang.repository.AdminProductLogRepository;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminProductLogEventListener {
    AdminProductLogRepository adminProductLogRepository;

    @Async("logExecutor")
    @EventListener
    public void handle(AdminProductLogEvent event) {
        try {
            adminProductLogRepository.save(AdminProductLogEvent.from(event));
        } catch (Exception e) {
            log.warn("Admin product log save failed", e);
        }
    }
}
