package store.onuljang.scheduler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import store.onuljang.appservice.AdminDeliveryAppService;
import store.onuljang.util.TimeUtil;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeliveryAutoCompleteScheduler {
    AdminDeliveryAppService adminDeliveryAppService;

    @Retryable(
        retryFor = {
            CannotAcquireLockException.class,
            PessimisticLockingFailureException.class,
            LockAcquisitionException.class,
        },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
    )
    @Scheduled(cron = "0 */30 * * * *", zone = "Asia/Seoul")
    public void autoCompleteDelivery() {
        LocalDateTime cutoff = TimeUtil.nowDateTime().minusMinutes(90);
        long updated = adminDeliveryAppService.processAutoCompleteDelivery(cutoff);
        if (updated > 0) {
            log.info("[DeliveryAutoCompleteScheduler] success: updated={}", updated);
        }
    }

    @Recover
    public void recover(Exception e) {
        log.error("[DeliveryAutoCompleteScheduler] job failed after retries: {}", e.getMessage(), e);
    }
}
