package store.onuljang.shared.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shared.auth.security.JwtUtil;
import store.onuljang.shared.auth.config.JwtConfigDto;
import store.onuljang.shared.auth.exception.InvalidRefreshTokenException;
import store.onuljang.shared.auth.repository.RefreshTokenRepository;
import store.onuljang.shared.auth.entity.RefreshToken;
import store.onuljang.shared.user.entity.Users;
import store.onuljang.shared.auth.dto.JwtToken;
import store.onuljang.shared.auth.util.CookieUtil;
import store.onuljang.shared.auth.util.HashUtil;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@Transactional(readOnly = true)
public class TokenService {
    RefreshTokenRepository refreshTokenRepository;
    JwtUtil jwtUtil;
    JwtConfigDto jwtConfigDto;
    HttpServletResponse httpServletResponse;

    @Transactional
    public String generateToken(Users user) {
        JwtToken jwtToken = jwtUtil.generateToken(user);

        refreshTokenRepository.save(
            RefreshToken.builder()
                .userUid(user.getUid())
                .tokenHash(HashUtil.sha256Hex(jwtToken.refresh()))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(jwtConfigDto.getRefreshTtl(), ChronoUnit.DAYS))
                .revoked(false)
                .build()
        );

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE,
                CookieUtil.generateStr(jwtToken.refresh(), jwtConfigDto.getRefreshTtl()));
        return jwtToken.access();
    }

    @Transactional
    public String generateToken(Users user, RefreshToken expriredToken) {
        JwtToken jwtToken = jwtUtil.generateToken(user);
        String tokenHash = HashUtil.sha256Hex(jwtToken.refresh());

        refreshTokenRepository.save(
            RefreshToken.builder()
                .userUid(user.getUid())
                .tokenHash(tokenHash)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(jwtConfigDto.getRefreshTtl(), ChronoUnit.DAYS))
                .revoked(false)
                .build()
        );

        expriredToken.rotateTo(tokenHash);

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE,
                CookieUtil.generateStr(jwtToken.refresh(), jwtConfigDto.getRefreshTtl()));
        return jwtToken.access();
    }

    @Transactional
    public RefreshToken validate(String userUid, String refreshToken) {
        String tokenHash = HashUtil.sha256Hex(refreshToken);

        Optional<RefreshToken> validToken = refreshTokenRepository
                .findByUserUidAndTokenHashAndRevokedIsFalseAndExpiresAtAfter(userUid, tokenHash, Instant.now());

        if (validToken.isPresent()) {
            RefreshToken token = validToken.get();
            if (!HashUtil.constantTimeEqualsHex(tokenHash, token.getTokenHash())) {
                throw new InvalidRefreshTokenException("refresh token hash mismatch");
            }
            return token;
        }

        Optional<RefreshToken> revokedToken = refreshTokenRepository
                .findByUserUidAndTokenHash(userUid, tokenHash);

        if (revokedToken.isPresent() && revokedToken.get().isRevoked()) {
            log.warn("Refresh token reuse detected for user: {}", userUid);
            refreshTokenRepository.revokeAllByUserUid(userUid);
            throw new InvalidRefreshTokenException("refresh token reuse detected");
        }

        throw new InvalidRefreshTokenException("refresh token not found or expired");
    }

}
