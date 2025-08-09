package store.onuljang.feign.dto;

import org.springframework.web.bind.annotation.RequestParam;

public record KakaoRequest(
    @RequestParam String grant_type,
    @RequestParam String client_id,
    @RequestParam String redirect_uri,
    @RequestParam String code
) {
}

