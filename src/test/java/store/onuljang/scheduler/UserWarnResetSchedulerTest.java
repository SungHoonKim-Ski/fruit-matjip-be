package store.onuljang.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.config.TestS3Config;
import store.onuljang.config.TestSqsConfig;
import store.onuljang.shop.reservation.scheduler.UserWarnResetScheduler;
import store.onuljang.shared.user.repository.UserRepository;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.support.TestFixture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserWarnResetScheduler 테스트
 *
 * 매달 1일 00:00에 모든 사용자의 경고 횟수를 0으로 초기화
 */
@SpringBootTest
@ActiveProfiles("test")
@Import({TestS3Config.class, TestSqsConfig.class})
@Transactional
class UserWarnResetSchedulerTest {

    @Autowired
    private UserWarnResetScheduler userWarnResetScheduler;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestFixture testFixture;

    @Test
    @DisplayName("경고 횟수 리셋 성공 - 모든 사용자 경고 횟수 0으로 초기화")
    void resetMonthlyWarnCount_Success() {
        // given
        Users user1 = testFixture.createUserWithWarns("유저1", 2);
        Users user2 = testFixture.createUserWithWarns("유저2", 3);
        Users user3 = testFixture.createUser("유저3"); // 경고 없음

        assertThat(user1.getMonthlyWarnCount()).isEqualTo(2);
        assertThat(user2.getMonthlyWarnCount()).isEqualTo(3);
        assertThat(user3.getMonthlyWarnCount()).isEqualTo(0);

        // when
        userWarnResetScheduler.resetMonthlyWarnCount();

        // then
        Users updatedUser1 = userRepository.findById(user1.getId()).orElseThrow();
        Users updatedUser2 = userRepository.findById(user2.getId()).orElseThrow();
        Users updatedUser3 = userRepository.findById(user3.getId()).orElseThrow();

        assertThat(updatedUser1.getMonthlyWarnCount()).isEqualTo(0);
        assertThat(updatedUser2.getMonthlyWarnCount()).isEqualTo(0);
        assertThat(updatedUser3.getMonthlyWarnCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("사용자가 없는 경우에도 정상 동작")
    void resetMonthlyWarnCount_NoUsers() {
        // when & then (예외 발생하지 않음)
        userWarnResetScheduler.resetMonthlyWarnCount();
    }
}
