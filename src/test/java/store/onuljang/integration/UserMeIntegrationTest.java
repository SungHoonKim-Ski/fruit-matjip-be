package store.onuljang.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import store.onuljang.controller.response.UserMeResponse;
import store.onuljang.repository.UserRepository;
import store.onuljang.repository.entity.Users;
import store.onuljang.support.IntegrationTestBase;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 사용자 정보 조회 API 통합 테스트
 *
 * API Spec:
 * - GET /api/auth/users/me - 현재 사용자 정보 조회
 */
class UserMeIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    private Users user;
    private String accessToken;

    @BeforeEach
    void setUp() {
        user = testFixture.createUser("테스트유저");
        accessToken = testFixture.createAccessToken(user);
    }

    @Nested
    @DisplayName("GET /api/auth/users/me - 사용자 정보 조회")
    class GetUserMe {

        @Test
        @DisplayName("정상 조회 시 닉네임과 이용제한 정보 반환")
        void getUserMe_Success() throws Exception {
            // when
            var response = getAction("/api/auth/users/me", accessToken, UserMeResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body()).isNotNull();
            assertThat(response.body().nickname()).isEqualTo(user.getName());
            assertThat(response.body().restricted()).isFalse();
            assertThat(response.body().restrictedUntil()).isNull();
        }

        @Test
        @DisplayName("이용제한 사용자 조회 시 restricted=true와 제한일 반환")
        void getUserMe_Restricted() throws Exception {
            // given
            LocalDate restrictedUntil = LocalDate.now().plusDays(7);
            user.restrict(restrictedUntil);
            userRepository.save(user);

            // when
            var response = getAction("/api/auth/users/me", accessToken, UserMeResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body()).isNotNull();
            assertThat(response.body().nickname()).isEqualTo(user.getName());
            assertThat(response.body().restricted()).isTrue();
            assertThat(response.body().restrictedUntil()).isEqualTo(restrictedUntil);
        }

        @Test
        @DisplayName("제한일이 지난 사용자는 restricted=false 반환")
        void getUserMe_RestrictedExpired() throws Exception {
            // given
            LocalDate pastDate = LocalDate.now().minusDays(1);
            user.restrict(pastDate);
            userRepository.save(user);

            // when
            var response = getAction("/api/auth/users/me", accessToken, UserMeResponse.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body()).isNotNull();
            assertThat(response.body().restricted()).isFalse();
            assertThat(response.body().restrictedUntil()).isEqualTo(pastDate);
        }

        @Test
        @DisplayName("인증 토큰이 없으면 401/403 반환")
        void getUserMe_Unauthorized() throws Exception {
            // when
            var response = getAction("/api/auth/users/me", UserMeResponse.class);

            // then
            assertThat(response.status()).isIn(401, 403);
        }
    }
}
