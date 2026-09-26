package com.gasmtask.auth.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Um elo da cadeia de refresh tokens de uma sessão. Só o hash é persistido.
 * Tokens de uma mesma sessão compartilham o {@code familyId}.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    private UUID id;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "revoked_reason", length = 20)
    private RevocationReason revokedReason;

    @Version
    private Long version;

    public static RefreshToken issue(UUID familyId, UUID userId, String tokenHash, Instant now, Duration ttl) {
        RefreshToken token = new RefreshToken();
        token.id = UUID.randomUUID();
        token.familyId = familyId;
        token.userId = userId;
        token.tokenHash = tokenHash;
        token.createdAt = now;
        token.expiresAt = now.plus(ttl);
        return token;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    /** Foi trocado por outro token há no máximo {@code window}? */
    public boolean wasRotatedWithin(Duration window, Instant now) {
        return revokedReason == RevocationReason.ROTATED
                && revokedAt != null
                && !revokedAt.plus(window).isBefore(now);
    }

    public void revoke(RevocationReason reason, Instant now) {
        if (revokedAt == null) {
            this.revokedAt = now;
            this.revokedReason = reason;
        }
    }
}
