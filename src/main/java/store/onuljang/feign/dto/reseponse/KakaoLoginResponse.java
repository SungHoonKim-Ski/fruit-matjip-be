package store.onuljang.feign.dto.reseponse;


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
