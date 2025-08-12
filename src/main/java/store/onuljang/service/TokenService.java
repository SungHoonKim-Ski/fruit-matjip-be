package store.onuljang.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.component.JwtUtil;
import store.onuljang.config.JwtConfigDto;
import store.onuljang.exception.InvalidRefreshTokenException;
import store.onuljang.exception.RefreshTokenNotFoundException;
import store.onuljang.exception.UserNotExistException;
import store.onuljang.exception.UserNotFoundException;
import store.onuljang.repository.RefreshTokenRepository;
import store.onuljang.repository.UserRepository;
import store.onuljang.repository.entity.RefreshToken;
import store.onuljang.repository.entity.Users;
import store.onuljang.service.dto.JwtToken;
import store.onuljang.util.CookieUtil;
import store.onuljang.util.HashUtil;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class TokenService {
    RefreshTokenRepository refreshTokenRepository;
    JwtUtil jwtUtil;
    JwtConfigDto jwtConfigDto;
    HttpServletResponse httpServletResponse;
    PasswordEncoder passwordEncoder;

    @Transactional
    public String generateToken(Users user) {
        JwtToken jwtToken = jwtUtil.generateToken(user);

        refreshTokenRepository.save(
            RefreshToken.builder()
                .userUid(user.getInternalUid())
                .tokenHash(HashUtil.sha256Hex(jwtToken.refresh()))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(jwtConfigDto.getRefreshTtl(), ChronoUnit.DAYS))
                .revoked(false)
                .build()
        );

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, CookieUtil.generateStr(jwtToken.refresh(), jwtConfigDto.getRefreshTtl()));
        return jwtToken.access();
    }

    @Transactional(readOnly = true)
    public RefreshToken validate(String userUid, String refreshToken) {
        String tokenHash = HashUtil.sha256Hex(refreshToken);

        RefreshToken token = refreshTokenRepository.findByUserUidAndTokenHashAndRevokedIsFalseAndExpiresAtAfter(
                userUid, tokenHash, Instant.now())
                .orElseThrow(() -> new InvalidRefreshTokenException("refresh token not found or expired"));

        if (!HashUtil.constantTimeEqualsHex(tokenHash, token.getTokenHash())) {
            throw new InvalidRefreshTokenException("refresh token hash mismatch");
        }
        return token;
    }


    @Transactional
    public String generateToken(Users user, RefreshToken expriredToken) {
        JwtToken jwtToken = jwtUtil.generateToken(user);
        String tokenHash = HashUtil.sha256Hex(jwtToken.refresh());

        refreshTokenRepository.save(
            RefreshToken.builder()
                .userUid(user.getInternalUid())
                .tokenHash(tokenHash)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(jwtConfigDto.getRefreshTtl(), ChronoUnit.DAYS))
                .revoked(false)
                .build()
        );

        expriredToken.rotateTo(tokenHash);

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, CookieUtil.generateStr(jwtToken.refresh(), jwtConfigDto.getRefreshTtl()));
        return jwtToken.access();
    }
}
