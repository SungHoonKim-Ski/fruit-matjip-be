package store.onuljang.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import store.onuljang.feign.dto.KakaoLoginResponse;

@FeignClient(name = "KAKAO-AUTH", url = "https://kauth.kakao.com")
public interface KakaoAuthFeignClient {

    @GetMapping(path = "/oauth/token", consumes = "application/x-www-form-urlencoded;charset=utf-8")
    KakaoLoginResponse login(
        @RequestParam("grant_type") String grantType,
        @RequestParam("client_id") String clientId,
        @RequestParam("redirect_uri") String redirectUri,
        @RequestParam("code") String code
    );
}
