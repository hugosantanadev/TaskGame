package com.gasmtask.auth.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.gasmtask.auth.domain.RefreshToken;
import com.gasmtask.auth.domain.RevocationReason;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update RefreshToken t
               set t.revokedAt = :now, t.revokedReason = :reason
             where t.familyId = :familyId and t.revokedAt is null
            """)
    int revokeFamily(@Param("familyId") UUID familyId,
                     @Param("reason") RevocationReason reason,
                     @Param("now") Instant now);

    @Modifying
    @Query("delete from RefreshToken t where t.expiresAt < :threshold")
    int deleteExpiredBefore(@Param("threshold") Instant threshold);
}
