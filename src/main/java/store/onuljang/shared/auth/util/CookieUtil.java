package store.onuljang.shared.auth.util;

import lombok.experimental.UtilityClass;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

@UtilityClass
public class CookieUtil {
    public String generateStr(String refreshToken, long refreshTtl) {
        return generate(refreshToken, refreshTtl).toString();
    }

    private ResponseCookie generate(String refreshToken, long refreshTtl) {
        return ResponseCookie.from("REFRESH_TOKEN", refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("Strict")
            .maxAge(Duration.ofDays(refreshTtl))
            .build();
    }
}
