package store.onuljang.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.config.TestS3Config;

import java.nio.charset.StandardCharsets;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * 통합 테스트 기반 클래스 H2 인메모리 DB를 사용하며, 각 테스트는 트랜잭션으로 격리됨
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestS3Config.class)
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected TestFixture testFixture;

    /**
     * API 응답을 상태 코드와 바디로 감싸는 레코드
     */
    public record ApiResponse<T>(int status, T body) {
        public boolean isOk() {
            return status == 200;
        }

        public boolean isCreated() {
            return status == 201;
        }

        public boolean isBadRequest() {
            return status == 400;
        }

        public boolean isUnauthorized() {
            return status == 401;
        }

        public boolean isForbidden() {
            return status == 403;
        }

        public boolean isNotFound() {
            return status == 404;
        }

        public boolean isNoContent() {
            return status == 204;
        }
    }

    /**
     * 공통 액션 수행
     */
    protected <T> ApiResponse<T> performAction(RequestBuilder requestBuilder, Class<T> responseType) throws Exception {
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        T body = null;
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        if (result.getResponse().getStatus() >= 400) {
            System.err.println("DEBUG: Request Failed. Status: " + result.getResponse().getStatus());
            System.err.println("DEBUG: Response Body: " + content);
        }

        if (responseType != null && responseType != Void.class && !content.isEmpty()) {
            body = objectMapper.readValue(content, responseType);
        }

        return new ApiResponse<>(result.getResponse().getStatus(), body);
    }

    /**
     * POST 요청 (인증 없음)
     */
    protected <T> ApiResponse<T> postAction(String uri, Object request, Class<T> responseType) throws Exception {
        return performAction(
                post(uri).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
                        .with(csrf()),
                responseType);
    }

    /**
     * POST 요청 (인증 헤더 포함)
     */
    protected <T> ApiResponse<T> postAction(String uri, Object request, String accessToken, Class<T> responseType)
            throws Exception {
        return performAction(post(uri).contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken).content(objectMapper.writeValueAsString(request)),
                responseType);
    }

    /**
     * PUT 요청 (인증 없음)
     */
    protected <T> ApiResponse<T> putAction(String uri, Object request, Class<T> responseType) throws Exception {
        return performAction(
                put(uri).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
                        .with(csrf()),
                responseType);
    }

    /**
     * PUT 요청 (인증 없음, 바디 없음 응답)
     */
    protected ApiResponse<Void> putAction(String uri, Object request) throws Exception {
        return performAction(
                put(uri).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
                        .with(csrf()),
                Void.class);
    }

    /**
     * GET 요청 (인증 없음)
     */
    protected <T> ApiResponse<T> getAction(String uri, Class<T> responseType) throws Exception {
        return performAction(get(uri).contentType(MediaType.APPLICATION_JSON), responseType);
    }

    /**
     * GET 요청 (인증 헤더 포함)
     */
    protected <T> ApiResponse<T> getAction(String uri, String accessToken, Class<T> responseType) throws Exception {
        return performAction(
                get(uri).contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + accessToken),
                responseType);
    }

    /**
     * PATCH 요청 (인증 헤더 포함, 바디 없음)
     */
    protected ApiResponse<Void> patchAction(String uri, String accessToken) throws Exception {
        return performAction(
                patch(uri).contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + accessToken),
                Void.class);
    }

    /**
     * PATCH 요청 (인증 헤더 포함, 바디 포함)
     */
    protected <T> ApiResponse<T> patchAction(String uri, Object request, String accessToken, Class<T> responseType)
            throws Exception {
        return performAction(patch(uri).contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken).content(objectMapper.writeValueAsString(request)),
                responseType);
    }

    /**
     * PATCH 요청 (관리자용, 인증 없음, 바디 포함)
     */
    protected <T> ApiResponse<T> patchAction(String uri, Object request, Class<T> responseType) throws Exception {
        return performAction(
                patch(uri).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
                        .with(csrf()),
                responseType);
    }

    /**
     * PATCH 요청 (관리자용, 인증 없음, 바디 없음)
     */
    protected ApiResponse<Void> patchAction(String uri) throws Exception {
        return performAction(patch(uri).contentType(MediaType.APPLICATION_JSON).with(csrf()), Void.class);
    }

    /**
     * DELETE 요청 (인증 헤더 포함)
     */
    protected ApiResponse<Void> deleteAction(String uri, String accessToken) throws Exception {
        return performAction(
                delete(uri).contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + accessToken),
                Void.class);
    }

    /**
     * DELETE 요청 (인증 없음)
     */
    protected ApiResponse<Void> deleteAction(String uri) throws Exception {
        return performAction(delete(uri).contentType(MediaType.APPLICATION_JSON).with(csrf()), Void.class);
    }

    /**
     * 관리자 인증 설정
     */
    protected void setAdminAuthentication(store.onuljang.repository.entity.Admin admin) {
        store.onuljang.service.dto.AdminUserDetails adminUserDetails = new store.onuljang.service.dto.AdminUserDetails(
                admin);
        store.onuljang.auth.AdminAuthenticationToken token = new store.onuljang.auth.AdminAuthenticationToken(
                adminUserDetails, null, adminUserDetails.getAuthorities(), admin.getId());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(token);
    }
}
