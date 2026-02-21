package store.onuljang.shared.auth.config;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@Getter
public class JwtConfigDto {
    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.access-ttl-minutes}")
    private Long accessTtl;

    @Value("${security.jwt.refresh-ttl-days}")
    private Long refreshTtl;

    private SecretKey keys;

    @PostConstruct
    void init() {
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        keys = Keys.hmacShaKeyFor(raw);
    }
}
