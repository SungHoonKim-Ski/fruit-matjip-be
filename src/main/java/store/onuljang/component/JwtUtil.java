package store.onuljang.component;

import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import store.onuljang.config.JwtConfigDto;
import store.onuljang.exception.AccessTokenParseException;
import store.onuljang.repository.entity.Users;
import store.onuljang.service.dto.JwtToken;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtUtil {
    JwtConfigDto jwtConfigDto;

    public JwtToken generateToken(Users user) {
        return generateToken(user.getInternalUid(), user.getName());
    }

    public JwtToken generateToken(String uid, String name) {
        Map<String, Object> claims = Map.of(
            "typ", "access",
            "uId", uid,
            "name",  name
        );

        String accessToken  = generateAccessToken(uid, claims);
        String refreshToken = generateRefreshToken(uid);

        return new JwtToken(accessToken, refreshToken);
    }

    public String getUid(String accessToken) {
        Jws<Claims> claims = parseAndValidate(accessToken);
        return claims.getBody().getSubject();
    }

    private String generateAccessToken(String subject, Map<String, Object> extraClaims) {
        return buildToken(subject, extraClaims, Instant.now().plus(jwtConfigDto.getAccessTtl(), ChronoUnit.MINUTES));
    }

    private String generateRefreshToken(String subject) {
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

    public Jws<Claims> parseAndValidate(String accessToken) throws JwtException {
        return Jwts.parserBuilder()
            .setSigningKey(jwtConfigDto.getKeys())
            .build()
            .parseClaimsJws(accessToken);
    }

    public String extractBearer(String auth){
        if (auth == null) {
            throw new AccessTokenParseException("Bearer token is null");
        }
        if (auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }

        throw new AccessTokenParseException("Bearer token is null");
    }
}