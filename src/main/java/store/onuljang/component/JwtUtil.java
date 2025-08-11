package store.onuljang.component;

import io.jsonwebtoken.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import store.onuljang.config.JwtConfigDto;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtUtil {
    JwtConfigDto jwtConfigDto;

    public String generateAccessToken(String subject, Map<String, Object> extraClaims) {
        return buildToken(subject, extraClaims, Instant.now().plus(jwtConfigDto.getAccessTtl(), ChronoUnit.MINUTES));
    }

    public String generateRefreshToken(String subject) {
        return buildToken(subject, Map.of("typ", "refresh"), Instant.now().plus(jwtConfigDto.getRefreshTtl(), ChronoUnit.DAYS));
    }

    private String buildToken(String subject, Map<String, Object> claims, Instant expiresAt) {
        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .addClaims(claims)
                .signWith(jwtConfigDto.getKeys())
                .compact();
    }

    public Jws<Claims> parseAndValidate(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(jwtConfigDto.getKeys())
                .build()
                .parseClaimsJws(token);
    }
}