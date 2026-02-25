package store.onuljang.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import store.onuljang.shop.admin.dto.AdminSignupRequest;
import store.onuljang.shop.admin.entity.Admin;
import store.onuljang.shop.admin.repository.AdminRepository;
import store.onuljang.support.IntegrationTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        @DisplayName("CSRF 토큰이 있으면 관리자 회원가입 성공")
        void adminSignup_Success() throws Exception {
            // given
            Admin owner = testFixture.createAdmin("소유자", "owner@test.com", "password123");
            setAdminAuthentication(owner);

            AdminSignupRequest request = new AdminSignupRequest("새관리자", "newadmin123", "password123");

            // when
            int status = mockMvc.perform(
                    post("/api/admin/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            ).andReturn().getResponse().getStatus();

            // then
            assertThat(status).isEqualTo(200);

            // DB 확인
            assertThat(adminRepository.findByEmail("newadmin123")).isPresent();
        }

        @Test
        @DisplayName("CSRF 토큰이 없으면 관리자 회원가입 실패(403)")
        void adminSignup_WithoutCsrf_Forbidden() throws Exception {
            // given
            Admin owner = testFixture.createAdmin("소유자", "owner-no-csrf@test.com", "password123");
            setAdminAuthentication(owner);

            AdminSignupRequest request = new AdminSignupRequest("새관리자", "newadmin-no-csrf", "password123");

            // when
            int status = mockMvc.perform(
                    post("/api/admin/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            ).andReturn().getResponse().getStatus();

            // then
            assertThat(status).isEqualTo(403);
            assertThat(adminRepository.findByEmail("newadmin-no-csrf")).isNotPresent();
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 회원가입 시 실패")
        void adminSignup_DuplicateEmail() throws Exception {
            // given
            Admin owner = testFixture.createAdmin("소유자", "owner@test.com", "password123");
            setAdminAuthentication(owner);

            testFixture.createAdmin("기존자", "existadmin", "password123");

            AdminSignupRequest request = new AdminSignupRequest("새관리자", "existadmin", "password123");

            // when
            int status = mockMvc.perform(
                    post("/api/admin/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            ).andReturn().getResponse().getStatus();

            // then
            assertThat(status).isEqualTo(409);
        }

        @Test
        @DisplayName("빈 이메일로 회원가입 시 실패")
        void adminSignup_EmptyEmail() throws Exception {
            // given
            Admin owner = testFixture.createAdmin("소유자", "owner@test.com", "password123");
            setAdminAuthentication(owner);

            AdminSignupRequest request = new AdminSignupRequest("새관리자", "", "password123");

            // when
            int status = mockMvc.perform(
                    post("/api/admin/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            ).andReturn().getResponse().getStatus();

            // then
            assertThat(status).withFailMessage("Expected status 400 but got " + status)
                    .isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/csrf - CSRF 토큰 발급")
    class CsrfToken {

        @Test
        @DisplayName("CSRF 토큰/헤더명/파라미터명을 반환한다")
        void issueCsrfToken() throws Exception {
            mockMvc.perform(get("/api/admin/csrf"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"))
                    .andExpect(jsonPath("$.parameterName").value("_csrf"));
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
