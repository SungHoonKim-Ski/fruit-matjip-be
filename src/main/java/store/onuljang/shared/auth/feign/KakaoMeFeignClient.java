package store.onuljang.shared.auth.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import store.onuljang.shared.auth.feign.dto.KakaoMeRespone;

@FeignClient(name = "KAKAO-ME", url = "https://kapi.kakao.com/v2")
public interface KakaoMeFeignClient {
    @GetMapping(path = "/user/me")
    public KakaoMeRespone getUser(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("Content-Type") String contentType
//        @RequestParam("property_keys") String property_keys
    );
}
