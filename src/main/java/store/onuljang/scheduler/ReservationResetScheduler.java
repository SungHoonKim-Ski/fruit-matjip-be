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
import store.onuljang.appservice.ReservationAppService;
import store.onuljang.repository.entity.enums.ReservationStatus;
import store.onuljang.util.TimeUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReservationResetScheduler {
    ReservationAppService reservationAppService;

    /**
     * 매일 19:05(KST) 실행.
     *   - retryFor: 재시도 대상으로 간주할 예외 목록.
     *               비관/낙관 락 충돌, DB 락 획득 실패, 동시 변경 검출(IllegalState) 등
     *   - backoff: 첫 시도 실패 후 1초 대기(기본), 이후 2배씩 증가(multiplier=2), 약간의 랜덤 지터 추가(random=true)
     *              (delay 기본값 1000ms, maxAttempts 기본값 3 → 명시하지 않으면 기본 적용)
     * 추후에 낙관적 락으로 변경할 경우, retryFor 내 관련 Exception 추가 필요
     */
    @Retryable(
        retryFor = {
            // DB 락을 바로 못 잡았을 때 (대기 타임아웃 등)
            CannotAcquireLockException.class,
            // JPA 비관적 락 충돌
            PessimisticLockingFailureException.class,
            // 하이버네이트 수준의 락 획득 실패
            LockAcquisitionException.class,
            // 도메인 논리상 동시 변경을 감지했을 때(예: 목표 건수와 실제 업데이트 건수 불일치)
            IllegalStateException.class
        },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
    )
    @Scheduled(cron = "0 3 20 * * *", zone = "Asia/Seoul")
    public void cancelNoShowDailyJob() {
        LocalDate today = TimeUtil.nowDate();
        LocalDateTime now = TimeUtil.nowDateTime();

        int updated = reservationAppService.cancelNoShow(today, ReservationStatus.PENDING, ReservationStatus.CANCELED, now);

        log.info("[ReservationResetScheduler] success: updated={} date={}", updated, today);
    }

    /**
     * 모든 재시도가 실패했을 때 호출
     */
    @Recover
    public void recover(Exception e) {
        log.error("[ReservationResetScheduler] job failed after retries: {}", e.getMessage(), e);
    }
}
