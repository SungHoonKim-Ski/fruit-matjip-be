package store.onuljang.shared.feign.dto.reseponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoPayReadyResponse(
    String tid,
    String nextRedirectPcUrl,
    String nextRedirectMobileUrl
) {}
