package store.onuljang.service;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store.onuljang.exception.KakaoPayException;
import store.onuljang.feign.KakaoPayFeignClient;
import store.onuljang.feign.dto.reseponse.KakaoPayOrderResponse;
import store.onuljang.support.IntegrationTestBase;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@TestPropertySource(properties = {
    "KAKAOPAY.ENABLED=false",
    "KAKAOPAY.SECRET_KEY=test-secret-key",
    "KAKAOPAY.CID=test-cid",
    "KAKAOPAY.APPROVAL_URL=http://localhost/approve",
    "KAKAOPAY.CANCEL_URL=http://localhost/cancel",
    "KAKAOPAY.FAIL_URL=http://localhost/fail",
    "KAKAOPAY.HOST=https://open-api.kakaopay.com"
})
class KakaoPayServiceOrderTest extends IntegrationTestBase {

    @Autowired
    KakaoPayService kakaoPayService;

    @MockitoBean
    KakaoPayFeignClient kakaoPayFeignClient;

    private Request dummyRequest() {
        return Request.create(Request.HttpMethod.POST, "/test",
            Collections.emptyMap(), null, new RequestTemplate());
    }

    @Test
    void order_success() {
        // Arrange
        KakaoPayOrderResponse expected = new KakaoPayOrderResponse(
            "T1234", "test-cid", "SUCCESS_PAYMENT",
            List.of(new KakaoPayOrderResponse.PaymentActionDetail("A123", "PAYMENT")));
        given(kakaoPayFeignClient.order(anyString(), any())).willReturn(expected);

        // Act
        KakaoPayOrderResponse result = kakaoPayService.order("T1234");

        // Assert
        assertThat(result.tid()).isEqualTo("T1234");
        assertThat(result.isSuccessPayment()).isTrue();
        assertThat(result.getApproveAid()).isEqualTo("A123");
    }

    @Test
    void order_feignException_throwsKakaoPayException() {
        // Arrange
        FeignException.InternalServerError serverError = new FeignException.InternalServerError(
            "Internal Server Error", dummyRequest(), null, null);
        given(kakaoPayFeignClient.order(anyString(), any())).willThrow(serverError);

        // Act & Assert
        assertThatThrownBy(() -> kakaoPayService.order("T1234"))
            .isInstanceOf(KakaoPayException.class)
            .hasMessageContaining("카카오페이 주문 조회에 실패했습니다.");
    }
}
