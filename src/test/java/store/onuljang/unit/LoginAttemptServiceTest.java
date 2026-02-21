package store.onuljang.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.onuljang.shared.auth.security.LoginAttemptService;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginAttemptService 단위 테스트")
class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService();
    }

    @Nested
    @DisplayName("로그인 실패 카운트")
    class LoginFailCount {

        @Test
        @DisplayName("5회 미만 실패 시 차단되지 않음")
        void underMaxAttempts_notBlocked() {
            // Arrange & Act
            for (int i = 0; i < 4; i++) {
                loginAttemptService.loginFailed("test@test.com");
            }

            // Assert
            assertThat(loginAttemptService.isBlocked("test@test.com")).isFalse();
        }

        @Test
        @DisplayName("5회 실패 시 차단됨")
        void atMaxAttempts_blocked() {
            // Arrange & Act
            for (int i = 0; i < 5; i++) {
                loginAttemptService.loginFailed("test@test.com");
            }

            // Assert
            assertThat(loginAttemptService.isBlocked("test@test.com")).isTrue();
        }

        @Test
        @DisplayName("5회 초과 실패 후에도 차단 유지")
        void overMaxAttempts_stillBlocked() {
            // Arrange & Act
            for (int i = 0; i < 10; i++) {
                loginAttemptService.loginFailed("test@test.com");
            }

            // Assert
            assertThat(loginAttemptService.isBlocked("test@test.com")).isTrue();
        }
    }

    @Nested
    @DisplayName("로그인 성공 시 초기화")
    class LoginSuccess {

        @Test
        @DisplayName("로그인 성공 시 실패 카운트 초기화")
        void loginSucceeded_resetsCount() {
            // Arrange
            for (int i = 0; i < 4; i++) {
                loginAttemptService.loginFailed("test@test.com");
            }

            // Act
            loginAttemptService.loginSucceeded("test@test.com");

            // Assert
            assertThat(loginAttemptService.isBlocked("test@test.com")).isFalse();

            // 다시 4회 실패해도 차단되지 않음
            for (int i = 0; i < 4; i++) {
                loginAttemptService.loginFailed("test@test.com");
            }
            assertThat(loginAttemptService.isBlocked("test@test.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("이메일별 독립 관리")
    class PerEmailIsolation {

        @Test
        @DisplayName("다른 이메일은 영향 없음")
        void differentEmails_independent() {
            // Arrange
            for (int i = 0; i < 5; i++) {
                loginAttemptService.loginFailed("blocked@test.com");
            }

            // Assert
            assertThat(loginAttemptService.isBlocked("blocked@test.com")).isTrue();
            assertThat(loginAttemptService.isBlocked("other@test.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("차단되지 않은 이메일")
    class NotBlocked {

        @Test
        @DisplayName("시도한 적 없는 이메일은 차단되지 않음")
        void neverAttempted_notBlocked() {
            // Assert
            assertThat(loginAttemptService.isBlocked("unknown@test.com")).isFalse();
        }
    }
}
