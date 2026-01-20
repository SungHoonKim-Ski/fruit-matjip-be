package store.onuljang.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import store.onuljang.controller.request.AdminSignupRequest;
import store.onuljang.repository.AdminRepository;
import store.onuljang.repository.entity.Admin;
import store.onuljang.support.IntegrationTestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 관리자 인증 API 통합 테스트
 *
 * API Spec: - POST /api/admin/signup - 관리자 회원가입 - GET /api/admin/validate - 세션
 * 검증
 */
class AdminControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private AdminRepository adminRepository;

    @Nested
    @DisplayName("POST /api/admin/signup - 관리자 회원가입")
    class AdminSignup {

        @Test
        @DisplayName("관리자 회원가입 성공")
        void adminSignup_Success() throws Exception {
            // given
            AdminSignupRequest request = new AdminSignupRequest("새관리자", "newadmin123", "password123");

            // when
            var response = postAction("/api/admin/signup", request, Void.class);

            // then
            assertThat(response.isOk()).isTrue();

            // DB 확인
            assertThat(adminRepository.findByEmail("newadmin123")).isPresent();
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 회원가입 시 실패")
        void adminSignup_DuplicateEmail() throws Exception {
            // given
            Admin existingAdmin = testFixture.createAdmin("기존자", "existadmin", "password123");

            AdminSignupRequest request = new AdminSignupRequest("새관리자", "existadmin", "password123");

            // when
            var response = postAction("/api/admin/signup", request, Void.class);

            // then
            assertThat(response.status()).isEqualTo(409);
        }

        @Test
        @DisplayName("빈 이메일로 회원가입 시 실패")
        void adminSignup_EmptyEmail() throws Exception {
            // given
            AdminSignupRequest request = new AdminSignupRequest("새관리자", "", "password123");

            // when
            var response = postAction("/api/admin/signup", request, Void.class);

            // then
            assertThat(response.status()).withFailMessage("Expected status 400 but got " + response.status())
                    .isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/validate - 세션 검증")
    class ValidateSession {

        @Test
        @DisplayName("미인증 상태에서 세션 검증 시 401 반환")
        void validateSession_Unauthorized() throws Exception {
            // when
            var response = getAction("/api/admin/validate", Void.class);

            // then
            assertThat(response.isUnauthorized()).isTrue();
        }
    }
}
