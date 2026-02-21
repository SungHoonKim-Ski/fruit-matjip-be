package store.onuljang.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import store.onuljang.shared.auth.security.LoginAttemptService;
import store.onuljang.shop.admin.repository.AdminRepository;
import store.onuljang.shop.admin.entity.Admin;
import store.onuljang.shop.admin.entity.AdminRole;
import store.onuljang.support.IntegrationTestBase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("관리자 로그인 brute force 차단 통합 테스트")
class AdminLoginBruteForceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginAttemptService loginAttemptService;

    private static int emailCounter = 0;

    private String testEmail;

    @BeforeEach
    void setUp() {
        testEmail = "brutetest" + (++emailCounter);
        Admin admin = Admin.builder()
                .name("테스트관리자")
                .email(testEmail)
                .password(passwordEncoder.encode("password1"))
                .role(AdminRole.OWNER)
                .build();
        adminRepository.saveAndFlush(admin);
    }

    @Test
    @DisplayName("5회 실패 후 올바른 비밀번호로도 로그인 차단")
    void bruteForce_blockedAfter5Failures() throws Exception {
        // Arrange - 5회 실패
        for (int i = 0; i < 5; i++) {
            loginRequest(testEmail, "wrongpass1");
        }

        // Act - 올바른 비밀번호로 시도
        // Assert - 차단됨 (401)
        mockMvc.perform(post("/api/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + testEmail + "\",\"password\":\"password1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("4회 실패 후 올바른 비밀번호로 로그인 성공")
    void underLimit_loginSucceeds() throws Exception {
        // Arrange - 4회 실패
        for (int i = 0; i < 4; i++) {
            loginRequest(testEmail, "wrongpass1");
        }

        // Act & Assert - 올바른 비밀번호로 성공
        mockMvc.perform(post("/api/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + testEmail + "\",\"password\":\"password1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인 성공 후 실패 카운트 초기화")
    void successResetsCounter() throws Exception {
        // Arrange - 4회 실패 후 성공
        for (int i = 0; i < 4; i++) {
            loginRequest(testEmail, "wrongpass1");
        }
        loginRequest(testEmail, "password1");

        // Act - 다시 4회 실패
        for (int i = 0; i < 4; i++) {
            loginRequest(testEmail, "wrongpass1");
        }

        // Assert - 아직 차단되지 않음 (올바른 비밀번호로 성공)
        mockMvc.perform(post("/api/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + testEmail + "\",\"password\":\"password1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 계정으로 5회 실패 시에도 차단")
    void nonExistentAccount_blockedAfter5Failures() throws Exception {
        // Arrange - 존재하지 않는 계정으로 5회 실패
        String fakeEmail = "fakeadmin" + (++emailCounter);
        for (int i = 0; i < 5; i++) {
            loginRequest(fakeEmail, "wrongpass1");
        }

        // Assert - 차단됨
        mockMvc.perform(post("/api/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + fakeEmail + "\",\"password\":\"password1\"}"))
                .andExpect(status().isUnauthorized());
    }

    private void loginRequest(String email, String password) throws Exception {
        mockMvc.perform(post("/api/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }
}
