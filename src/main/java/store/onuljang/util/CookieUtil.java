package store.onuljang.util;

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
            .secure(false)
            .path("/")
            .sameSite("Strict")
            .maxAge(Duration.ofDays(refreshTtl))
            .build();
    }
}
