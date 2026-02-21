package store.onuljang.shared.auth.feign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoMeRespone(
    String id,
    Properties properties,
    @JsonProperty("kakao_account")
    KakaoAccount kakaoAccount
) {
    public record Properties(
        String nickname
    ) {}

    public record KakaoAccount(
        Profile profile,
        String email
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Profile(
        String nickname
    ) {}
}
