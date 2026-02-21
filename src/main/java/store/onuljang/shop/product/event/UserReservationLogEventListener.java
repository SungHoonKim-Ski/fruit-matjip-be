package store.onuljang.shop.product.event;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import store.onuljang.shared.user.repository.UserReservationLogRepository;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserReservationLogEventListener {
    UserReservationLogRepository userReservationLogRepository;

    @Async("logExecutor")
    @EventListener
    public void handle(UserReservationLogEvent event) {
        try {
            userReservationLogRepository.save(UserReservationLogEvent.from(event));
        } catch (Exception e) {
            log.warn("user reservation log save failed", e);
        }
    }
}
