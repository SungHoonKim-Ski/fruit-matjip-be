package store.onuljang.service;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store.onuljang.shared.exception.KakaoPayException;
import store.onuljang.shared.feign.KakaoPayFeignClient;
import store.onuljang.shared.feign.dto.request.KakaoPayApproveRequest;
import store.onuljang.shared.feign.dto.reseponse.KakaoPayApproveResponse;
import store.onuljang.support.IntegrationTestBase;
import store.onuljang.shared.service.KakaoPayService;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@TestPropertySource(properties = {
    "KAKAOPAY.ENABLED=false",
    "KAKAOPAY.SECRET_KEY=test-secret-key",
    "KAKAOPAY.CID=test-cid",
    "KAKAOPAY.APPROVAL_URL=http://localhost/approve",
    "KAKAOPAY.CANCEL_URL=http://localhost/cancel",
    "KAKAOPAY.FAIL_URL=http://localhost/fail",
    "KAKAOPAY.HOST=https://open-api.kakaopay.com"
})
class KakaoPayServiceRetryTest extends IntegrationTestBase {

    @Autowired
    KakaoPayService kakaoPayService;

    @MockitoBean
    KakaoPayFeignClient kakaoPayFeignClient;

    private static final KakaoPayApproveRequest REQUEST =
        new KakaoPayApproveRequest(null, "T1234", "1", "uid", "pg_token");

    private Request dummyRequest() {
        return Request.create(Request.HttpMethod.POST, "/test",
            Collections.emptyMap(), null, new RequestTemplate());
    }

    @Test
    void approve_success() {
        // Arrange
        KakaoPayApproveResponse expected = new KakaoPayApproveResponse(
            "A123", "T1234", "TC0ONETIME", "1", "uid", "MONEY", "딸기", "2026-01-21T10:00:00");
        given(kakaoPayFeignClient.approve(anyString(), any())).willReturn(expected);

        // Act
        KakaoPayApproveResponse result = kakaoPayService.approve(REQUEST);

        // Assert
        assertThat(result.aid()).isEqualTo("A123");
        verify(kakaoPayFeignClient, times(1)).approve(anyString(), any());
    }

    @Test
    void approve_4xx_failsImmediately_withParsedMessage() {
        // Arrange
        String errorBody = """
            {"error_code":-780,"error_message":"approval failure!","extras":{"method_result_code":"USER_LOCKED","method_result_message":"진행중인 거래가 있습니다. 잠시 후 다시 시도해 주세요."}}""";
        FeignException.BadRequest badRequest = new FeignException.BadRequest(
            "Bad Request", dummyRequest(), errorBody.getBytes(StandardCharsets.UTF_8), null);
        given(kakaoPayFeignClient.approve(anyString(), any())).willThrow(badRequest);

        // Act & Assert
        // 4xx는 approve()에서 catch되어 KakaoPayException으로 변환되므로 재시도 없이 즉시 실패
        assertThatThrownBy(() -> kakaoPayService.approve(REQUEST))
            .isInstanceOf(KakaoPayException.class)
            .hasMessageContaining("진행중인 거래가 있습니다");

        // 4xx는 재시도 없이 1회만 호출
        verify(kakaoPayFeignClient, times(1)).approve(anyString(), any());
    }

    @Test
    void approve_5xx_retriesThenSucceeds() {
        // Arrange
        FeignException.InternalServerError serverError = new FeignException.InternalServerError(
            "Internal Server Error", dummyRequest(), null, null);
        KakaoPayApproveResponse expected = new KakaoPayApproveResponse(
            "A456", "T1234", "TC0ONETIME", "1", "uid", "CARD", "사과", "2026-01-21T10:00:00");
        given(kakaoPayFeignClient.approve(anyString(), any()))
            .willThrow(serverError)
            .willReturn(expected);

        // Act
        KakaoPayApproveResponse result = kakaoPayService.approve(REQUEST);

        // Assert
        assertThat(result.aid()).isEqualTo("A456");
        verify(kakaoPayFeignClient, times(2)).approve(anyString(), any());
    }

    @Test
    void approve_5xx_exhaustsRetries_throwsKakaoPayException() {
        // Arrange
        FeignException.InternalServerError serverError = new FeignException.InternalServerError(
            "Internal Server Error", dummyRequest(), null, null);
        given(kakaoPayFeignClient.approve(anyString(), any())).willThrow(serverError);

        // Act & Assert
        assertThatThrownBy(() -> kakaoPayService.approve(REQUEST))
            .isInstanceOf(KakaoPayException.class)
            .hasMessageContaining("잠시 후 다시 시도해주세요");

        // maxAttempts=3이므로 3번 호출
        verify(kakaoPayFeignClient, times(3)).approve(anyString(), any());
    }
}
