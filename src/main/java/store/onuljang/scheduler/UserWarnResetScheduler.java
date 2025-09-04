package store.onuljang.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserWarnResetScheduler {

    private final UserRepository usersRepository;

    /**
     * 매달 1일 00:05 KST에 warn_count를 0으로 초기화
     */
    @Transactional
    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Seoul")
    public void resetMonthlyWarnCount() {
        int updated = usersRepository.resetAllUsersWarnCounts();
        log.info("[WarnCountResetScheduler] reset done. updated rows = {}", updated);
    }
}