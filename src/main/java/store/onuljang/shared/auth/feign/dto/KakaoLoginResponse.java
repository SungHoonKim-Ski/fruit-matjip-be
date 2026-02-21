package store.onuljang.shared.auth.feign.dto;


public record KakaoLoginResponse(
    String tokenType,
    String accessToken,
    String refreshToken,
    String idToken,
    Integer expiresIn,
    Integer refreshTokenExpiresIn
) {
    public String getBearerAccessToken() {
        return tokenType + " " + accessToken;
    }
}
