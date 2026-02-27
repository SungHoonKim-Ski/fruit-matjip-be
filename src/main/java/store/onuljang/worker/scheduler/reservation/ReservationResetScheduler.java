package store.onuljang.worker.scheduler.reservation;

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
import store.onuljang.shop.admin.appservice.AdminReservationAppService;
import store.onuljang.shop.reservation.config.StoreConfigSnapshot;
import store.onuljang.shop.reservation.service.StoreConfigService;
import store.onuljang.shared.util.TimeUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReservationResetScheduler {
    AdminReservationAppService adminReservationAppService;
    StoreConfigService storeConfigService;

    /**
     * 10분 주기 폴링 + guard check.
     * StoreConfig의 pickupDeadline + 3분 버퍼 이후에만 노쇼 배치 실행.
     * 멱등성: PENDING → NO_SHOW는 중복 실행해도 안전 (이미 NO_SHOW면 대상 없음)
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
    @Scheduled(cron = "0 */10 * * * *", zone = "Asia/Seoul")
    public void processNoShowBatch() {
        StoreConfigSnapshot config = storeConfigService.getConfig();
        LocalDate sellDate = resolveCurrentSellDate(config);

        ZonedDateTime deadline = TimeUtil.resolveDeadline(
            sellDate, config.pickupDeadlineHour(), config.pickupDeadlineMinute());
        if (TimeUtil.nowZonedDateTime().isBefore(deadline.plusMinutes(3))) {
            return;
        }

        LocalDateTime now = TimeUtil.nowDateTime();
        long updated = adminReservationAppService.processNoShowBatch(sellDate, now);

        if (updated > 0) {
            log.info("[ReservationResetScheduler] success: updated={} sellDate={}", updated, sellDate);
        }
    }

    /**
     * 모든 재시도가 실패했을 때 호출
     */
    @Recover
    public void recover(Exception e) {
        log.error("[ReservationResetScheduler] job failed after retries: {}", e.getMessage(), e);
    }

    /**
     * cross-midnight 대응: 현재가 자정~(마감시각+버퍼) 사이면 어제의 영업일.
     */
    private LocalDate resolveCurrentSellDate(StoreConfigSnapshot config) {
        LocalDate today = TimeUtil.nowDate();
        if (config.pickupDeadlineHour() >= 24) {
            int overflowHour = config.pickupDeadlineHour() - 24;
            ZonedDateTime now = TimeUtil.nowZonedDateTime();
            int nowTotal = now.getHour() * 60 + now.getMinute();
            int overflowTotal = overflowHour * 60 + config.pickupDeadlineMinute();
            if (nowTotal <= overflowTotal + 10) {
                return today.minusDays(1);
            }
        }
        return today;
    }
}
