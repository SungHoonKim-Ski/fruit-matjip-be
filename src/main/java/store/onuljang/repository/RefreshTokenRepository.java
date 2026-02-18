package store.onuljang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.onuljang.repository.entity.RefreshToken;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUserUidAndTokenHashAndRevokedIsFalseAndExpiresAtAfter(String userUid, String tokenHash,
            Instant expiresAt);

    Optional<RefreshToken> findByUserUidAndTokenHash(String userUid, String tokenHash);

    @Modifying
    @Query("UPDATE RefreshToken t SET t.revoked = true WHERE t.userUid = :userUid AND t.revoked = false")
    int revokeAllByUserUid(@Param("userUid") String userUid);
}
