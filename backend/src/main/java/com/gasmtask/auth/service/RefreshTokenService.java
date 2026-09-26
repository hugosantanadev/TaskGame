package com.gasmtask.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import com.gasmtask.auth.config.RefreshTokenProperties;
import com.gasmtask.auth.domain.RefreshToken;
import com.gasmtask.auth.domain.RevocationReason;
import com.gasmtask.auth.repository.RefreshTokenRepository;
import com.gasmtask.shared.exception.BusinessException;
import com.gasmtask.shared.exception.ErrorCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Refresh tokens opacos com rotação e detecção de reuso.
 *
 * <ul>
 *   <li>Cada login abre uma família (sessão); cada renovação troca o token por outro da mesma família.</li>
 *   <li>Se um token já trocado reaparece fora da janela de tolerância, alguém o copiou:
 *       a família inteira é revogada e o dono precisa entrar de novo.</li>
 *   <li>Logout revoga a família da sessão atual, sem derrubar outros dispositivos.</li>
 * </ul>
 */
@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);
    private static final int TOKEN_BYTES = 32;

    private final RefreshTokenRepository repository;
    private final RefreshTokenProperties properties;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository repository, RefreshTokenProperties properties, Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public IssuedRefreshToken startSession(UUID userId) {
        return issue(UUID.randomUUID(), userId, clock.instant());
    }

    /**
     * Troca o token apresentado por um novo. {@code noRollbackFor}: a revogação por reuso precisa
     * ser gravada mesmo com a exceção que devolve 401.
     */
    @Transactional(noRollbackFor = BusinessException.class)
    public Rotation rotate(String rawToken) {
        Instant now = clock.instant();
        RefreshToken current = findByRawToken(rawToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (current.isRevoked()) {
            if (current.wasRotatedWithin(properties.reuseGracePeriod(), now)) {
                return new Rotation(current.getUserId(), issue(current.getFamilyId(), current.getUserId(), now));
            }
            log.warn("Refresh token revogado foi reapresentado; revogando a sessão {}", current.getFamilyId());
            repository.revokeFamily(current.getFamilyId(), RevocationReason.REUSE_DETECTED, now);
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (current.isExpired(now)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        current.revoke(RevocationReason.ROTATED, now);
        return new Rotation(current.getUserId(), issue(current.getFamilyId(), current.getUserId(), now));
    }

    @Transactional
    public void endSession(String rawToken) {
        findByRawToken(rawToken).ifPresent(token ->
                repository.revokeFamily(token.getFamilyId(), RevocationReason.LOGOUT, clock.instant()));
    }

    @Transactional
    public int purgeExpired(Duration retention) {
        return repository.deleteExpiredBefore(clock.instant().minus(retention));
    }

    private IssuedRefreshToken issue(UUID familyId, UUID userId, Instant now) {
        String rawToken = generateRawToken();
        RefreshToken token = RefreshToken.issue(familyId, userId, hash(rawToken), now, properties.ttl());
        repository.save(token);
        return new IssuedRefreshToken(rawToken, token.getExpiresAt());
    }

    private Optional<RefreshToken> findByRawToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        return repository.findByTokenHash(hash(rawToken));
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível na JVM", e);
        }
    }

    /** Valor bruto do token (vai só para o cookie) e sua expiração. */
    public record IssuedRefreshToken(String value, Instant expiresAt) {
    }

    public record Rotation(UUID userId, IssuedRefreshToken refreshToken) {
    }
}
