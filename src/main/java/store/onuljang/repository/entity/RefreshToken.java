package store.onuljang.repository.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_uid", nullable = false, length = 36)
    private String userUid;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "replaced_by", length = 64)
    private String replacedBy;

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }

    public void revoke() {
        this.revoked = true;
    }

    public void rotateTo(String newTokenHash) {
        this.revoked = true;
        this.replacedBy = newTokenHash;
    }
}