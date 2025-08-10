package store.onuljang.feign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import store.onuljang.controller.response.LoginResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoMeRespone(
    Long id,
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

