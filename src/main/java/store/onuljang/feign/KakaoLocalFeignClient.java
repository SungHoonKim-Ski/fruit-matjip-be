package store.onuljang.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import store.onuljang.feign.dto.reseponse.KakaoLocalAddressSearchResponse;

@FeignClient(name = "KAKAO-LOCAL", url = "${KAKAO.LOCAL_HOST:https://dapi.kakao.com}")
public interface KakaoLocalFeignClient {
    @GetMapping(path = "/v2/local/search/address.json")
    KakaoLocalAddressSearchResponse searchAddress(
        @RequestHeader("Authorization") String authorization,
        @RequestParam("query") String query
    );
}
