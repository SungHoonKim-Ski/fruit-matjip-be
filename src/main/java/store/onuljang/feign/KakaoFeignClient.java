package store.onuljang.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "KAKAO", url = "https://kauth.kakao.com/oauth/token")
public interface KakaoFeignClient {

    @PostMapping(consumes = "application/x-www-form-urlencoded;charset=utf-8")
    public String getToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("code") String code
    );
}
