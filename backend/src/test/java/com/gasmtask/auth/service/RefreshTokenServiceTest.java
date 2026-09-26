package com.gasmtask.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import com.gasmtask.auth.config.RefreshTokenProperties;
import com.gasmtask.auth.domain.RefreshToken;
import com.gasmtask.auth.domain.RevocationReason;
import com.gasmtask.auth.repository.RefreshTokenRepository;
import com.gasmtask.auth.service.RefreshTokenService.IssuedRefreshToken;
import com.gasmtask.auth.service.RefreshTokenService.Rotation;
import com.gasmtask.shared.exception.BusinessException;
import com.gasmtask.shared.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-23T12:00:00Z");
    private static final Duration TTL = Duration.ofDays(30);
    private static final Duration GRACE = Duration.ofSeconds(10);
    private static final UUID USER = UUID.randomUUID();
    private static final UUID FAMILY = UUID.randomUUID();

    @Mock
    private RefreshTokenRepository repository;

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        RefreshTokenProperties properties =
                new RefreshTokenProperties(TTL, GRACE, "gt_refresh", "/api/v1/auth", true, "0 30 3 * * *");
        service = new RefreshTokenService(repository, properties, Clock.fixed(NOW, ZoneOffset.UTC));
        lenient().when(repository.save(any(RefreshToken.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void startSessionPersistsOnlyTheHashOfTheToken() {
        IssuedRefreshToken issued = service.startSession(USER);

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getTokenHash())
                .isEqualTo(RefreshTokenService.hash(issued.value()))
                .isNotEqualTo(issued.value());
        assertThat(saved.getValue().getUserId()).isEqualTo(USER);
        assertThat(issued.expiresAt()).isEqualTo(NOW.plus(TTL));
    }

    @Test
    void rotateRevokesTheCurrentTokenAndIssuesTheNextOneInTheSameSession() {
        RefreshToken current = storedToken("token-atual", NOW.minus(Duration.ofDays(1)));

        Rotation rotation = service.rotate("token-atual");

        assertThat(current.getRevokedReason()).isEqualTo(RevocationReason.ROTATED);
        assertThat(current.getRevokedAt()).isEqualTo(NOW);
        assertThat(rotation.userId()).isEqualTo(USER);
        assertThat(rotation.refreshToken().value()).isNotEqualTo("token-atual");
        verify(repository).save(argThat(next -> next.getFamilyId().equals(FAMILY) && !next.isRevoked()));
    }

    @Test
    void rotateRejectsAnExpiredToken() {
        storedToken("token-velho", NOW.minus(TTL).minusSeconds(1));

        assertInvalidRefreshToken("token-velho");
        verify(repository, never()).save(any());
    }

    @Test
    void rotateRejectsAnUnknownToken() {
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertInvalidRefreshToken("token-que-nao-existe");
    }

    @Test
    void reuseRightAfterRotationIsToleratedWithinTheGracePeriod() {
        RefreshToken current = storedToken("recem-trocado", NOW.minus(Duration.ofHours(1)));
        current.revoke(RevocationReason.ROTATED, NOW.minusSeconds(5));

        Rotation rotation = service.rotate("recem-trocado");

        assertThat(rotation.userId()).isEqualTo(USER);
        verify(repository, never()).revokeFamily(any(), any(), any());
    }

    @Test
    void reuseAfterTheGracePeriodRevokesTheWholeSession() {
        RefreshToken current = storedToken("copiado", NOW.minus(Duration.ofHours(1)));
        current.revoke(RevocationReason.ROTATED, NOW.minusSeconds(30));

        assertInvalidRefreshToken("copiado");
        verify(repository).revokeFamily(FAMILY, RevocationReason.REUSE_DETECTED, NOW);
        verify(repository, never()).save(any());
    }

    @Test
    void aTokenEndedByLogoutIsNeverAcceptedAgain() {
        RefreshToken current = storedToken("apos-logout", NOW.minus(Duration.ofHours(1)));
        current.revoke(RevocationReason.LOGOUT, NOW.minusSeconds(1));

        assertInvalidRefreshToken("apos-logout");
        verify(repository, never()).save(any());
    }

    @Test
    void endSessionRevokesEveryTokenOfTheSession() {
        storedToken("sessao-atual", NOW.minus(Duration.ofHours(1)));

        service.endSession("sessao-atual");

        verify(repository).revokeFamily(FAMILY, RevocationReason.LOGOUT, NOW);
    }

    @Test
    void endSessionIgnoresMissingToken() {
        service.endSession(null);

        verify(repository, never()).revokeFamily(any(), any(), any());
    }

    private RefreshToken storedToken(String rawValue, Instant createdAt) {
        RefreshToken token = RefreshToken.issue(FAMILY, USER, RefreshTokenService.hash(rawValue), createdAt, TTL);
        when(repository.findByTokenHash(RefreshTokenService.hash(rawValue))).thenReturn(Optional.of(token));
        return token;
    }

    private void assertInvalidRefreshToken(String rawValue) {
        assertThatThrownBy(() -> service.rotate(rawValue))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.code()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN));
    }
}
