package store.onuljang.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import store.onuljang.feign.dto.request.KakaoPayApproveRequest;
import store.onuljang.feign.dto.request.KakaoPayCancelRequest;
import store.onuljang.feign.dto.request.KakaoPayReadyRequest;
import store.onuljang.feign.dto.reseponse.KakaoPayApproveResponse;
import store.onuljang.feign.dto.reseponse.KakaoPayCancelResponse;
import store.onuljang.feign.dto.reseponse.KakaoPayReadyResponse;

@FeignClient(name = "KAKAO-PAY", url = "${KAKAOPAY.HOST}")
public interface KakaoPayFeignClient {
    @PostMapping(path = "/online/v1/payment/ready", consumes = "application/json")
    KakaoPayReadyResponse ready(
        @RequestHeader("Authorization") String authorization,
        @RequestBody KakaoPayReadyRequest body
    );

    @PostMapping(path = "/online/v1/payment/approve", consumes = "application/json")
    KakaoPayApproveResponse approve(
        @RequestHeader("Authorization") String authorization,
        @RequestBody KakaoPayApproveRequest body
    );

    @PostMapping(path = "/online/v1/payment/cancel", consumes = "application/json")
    KakaoPayCancelResponse cancel(
        @RequestHeader("Authorization") String authorization,
        @RequestBody KakaoPayCancelRequest body
    );
}
