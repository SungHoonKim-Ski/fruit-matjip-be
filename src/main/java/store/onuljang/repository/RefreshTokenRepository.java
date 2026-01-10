package store.onuljang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.onuljang.repository.entity.RefreshToken;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUserUidAndTokenHashAndRevokedIsFalseAndExpiresAtAfter(String userUid, String tokenHash,
            Instant expiresAt);
}
