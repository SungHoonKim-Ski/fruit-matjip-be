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
import store.onuljang.appservice.AdminAggregationAppService;
import store.onuljang.appservice.AdminReservationAppService;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReservationAggregationScheduler {
     AdminAggregationAppService adminAggregationAppService;

    /**
     * 매일 00:10(KST) 실행.
     */
    @Retryable(
        retryFor = {
            // DB 락을 바로 못 잡았을 때 (대기 타임아웃 등)
            CannotAcquireLockException.class,
            // JPA 비관적 락 충돌
            PessimisticLockingFailureException.class,
            // 하이버네이트 수준의 락 획득 실패
            LockAcquisitionException.class,
        },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
    )
    @Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul")
    public void aggregate() {
        adminAggregationAppService.aggregateReservation();
    }

    /**
     * 모든 재시도가 실패했을 때 호출
     */
    @Recover
    public void recover(Exception e) {
        log.error("[Aggregation] job failed after retries: {}", e.getMessage(), e);
    }
}