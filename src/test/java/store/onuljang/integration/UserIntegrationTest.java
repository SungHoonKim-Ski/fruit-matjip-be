package store.onuljang.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import store.onuljang.repository.UserRepository;
import store.onuljang.repository.entity.Users;
import store.onuljang.support.IntegrationTestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 사용자 정보 API 통합 테스트
 *
 * API Spec: - PATCH /api/auth/name/{name} - 닉네임 변경 - GET /api/auth/name/{name}
 * - 닉네임 존재 확인 - GET /api/auth/message - 메시지 조회
 * - PATCH /api/auth/message/{messageId} - 메시지 수신 확인
 */
class UserIntegrationTest extends IntegrationTestBase {

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
    @DisplayName("PATCH /api/auth/name/{name} - 닉네임 변경")
    class ModifyName {

        @Test
        @DisplayName("닉네임 변경 성공")
        void modifyName_Success() throws Exception {
            // given
            String newName = "새로운닉네임";

            // when
            var response = patchAction("/api/auth/name/" + newName, accessToken);

            // then
            assertThat(response.isOk()).isTrue();

            // 변경 확인
            Users updatedUser = userRepository.findById(user.getId()).orElseThrow();
            assertThat(updatedUser.getName()).isEqualTo(newName);
        }

        @Test
        @DisplayName("이미 존재하는 닉네임으로 변경 시 실패")
        void modifyName_DuplicateName() throws Exception {
            // given
            testFixture.createUserWithExactName("중복닉네임");

            // when
            var response = patchAction("/api/auth/name/중복닉네임", accessToken);

            // then
            assertThat(response.status()).isEqualTo(409);
        }

        @Test
        @DisplayName("닉네임이 3자 미만인 경우 실패")
        void modifyName_TooShort() throws Exception {
            // when
            var response = patchAction("/api/auth/name/AB", accessToken);

            // then
            assertThat(response.isBadRequest()).isTrue();
        }

        @Test
        @DisplayName("닉네임이 10자 초과인 경우 실패")
        void modifyName_TooLong() throws Exception {
            // when
            var response = patchAction("/api/auth/name/12345678901", accessToken);

            // then
            assertThat(response.isBadRequest()).isTrue();
        }

        @Test
        @DisplayName("닉네임에 특수문자 포함 시 실패")
        void modifyName_SpecialCharacters() throws Exception {
            // when
            var response = patchAction("/api/auth/name/테스트@닉", accessToken);

            // then
            assertThat(response.isBadRequest()).isTrue();
        }

        @Test
        @DisplayName("한글, 영어, 숫자 조합 닉네임 성공")
        void modifyName_ValidCharacters() throws Exception {
            // when
            var response = patchAction("/api/auth/name/테스트123", accessToken);

            // then
            assertThat(response.isOk()).isTrue();
        }
    }

    @Nested
    @DisplayName("GET /api/auth/name/{name} - 닉네임 존재 확인")
    class CheckNameExists {

        @Test
        @DisplayName("사용 가능한 닉네임인 경우 true 반환")
        void existName_Available() throws Exception {
            // when
            var response = getAction("/api/auth/name/새닉네임", accessToken, Boolean.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body()).isTrue();
        }

        @Test
        @DisplayName("이미 존재하는 닉네임인 경우 false 반환")
        void existName_AlreadyExists() throws Exception {
            // given
            testFixture.createUserWithExactName("중복닉네임");

            // when
            var response = getAction("/api/auth/name/중복닉네임", accessToken, Boolean.class);

            // then
            assertThat(response.isOk()).isTrue();
            assertThat(response.body()).isFalse();
        }
    }

    @Nested
    @DisplayName("GET /api/auth/message - 메시지 조회")
    class GetMessage {

        @Test
        @DisplayName("메시지가 없는 경우 204 반환")
        void getMessage_NoContent() throws Exception {
            // when
            var response = getAction("/api/auth/message", accessToken, Void.class);

            // then
            assertThat(response.isNoContent()).isTrue();
        }
    }
}
