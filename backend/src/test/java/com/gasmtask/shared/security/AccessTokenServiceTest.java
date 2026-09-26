package com.gasmtask.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.gasmtask.shared.security.AccessTokenService.IssuedAccessToken;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;

class AccessTokenServiceTest {

    private static final String SECRET = "unit-test-secret-with-more-than-32-bytes";
    private static final Duration TTL = Duration.ofMinutes(15);

    private final JwtConfig jwtConfig = new JwtConfig();
    private final JwtProperties properties = new JwtProperties(SECRET, "gasmtask", TTL);

    @Test
    void issuesATokenWhoseSubjectIsTheUser() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        UUID userId = UUID.randomUUID();

        IssuedAccessToken issued = serviceAt(now, properties).issue(userId);
        Jwt jwt = jwtConfig.jwtDecoder(properties).decode(issued.value());

        assertThat(jwt.getSubject()).isEqualTo(userId.toString());
        assertThat(jwt.getClaimAsString(JwtClaimNames.ISS)).isEqualTo("gasmtask");
        assertThat(jwt.getExpiresAt()).isEqualTo(now.plus(TTL));
        assertThat(issued.expiresAt()).isEqualTo(now.plus(TTL));
    }

    @Test
    void rejectsATokenFromAnotherIssuer() {
        JwtProperties otherIssuer = new JwtProperties(SECRET, "outro-sistema", TTL);
        String token = serviceAt(Instant.now(), otherIssuer).issue(UUID.randomUUID()).value();

        assertThatThrownBy(() -> jwtConfig.jwtDecoder(properties).decode(token))
                .isInstanceOf(JwtValidationException.class);
    }

    @Test
    void rejectsATokenSignedWithAnotherSecret() {
        JwtProperties otherSecret = new JwtProperties("another-secret-that-also-has-32-bytes!!", "gasmtask", TTL);
        String token = serviceAt(Instant.now(), otherSecret).issue(UUID.randomUUID()).value();

        assertThatThrownBy(() -> jwtConfig.jwtDecoder(properties).decode(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsAnExpiredToken() {
        String token = serviceAt(Instant.now().minus(Duration.ofHours(1)), properties)
                .issue(UUID.randomUUID()).value();

        assertThatThrownBy(() -> jwtConfig.jwtDecoder(properties).decode(token))
                .isInstanceOf(JwtValidationException.class);
    }

    private AccessTokenService serviceAt(Instant now, JwtProperties props) {
        return new AccessTokenService(jwtConfig.jwtEncoder(props), props, Clock.fixed(now, ZoneOffset.UTC));
    }
}
