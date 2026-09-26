package com.gasmtask.shared.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração do access token (JWT assinado com HS256).
 *
 * @param secret         segredo compartilhado; precisa ter pelo menos 32 bytes (256 bits) para HS256
 * @param issuer         valor do claim {@code iss}, conferido na validação
 * @param accessTokenTtl validade do access token
 */
@ConfigurationProperties("app.security.jwt")
public record JwtProperties(String secret, String issuer, Duration accessTokenTtl) {

    private static final int MIN_SECRET_BYTES = 32;

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "Defina app.security.jwt.secret (variável JWT_SECRET) com pelo menos 32 bytes.");
        }
        Objects.requireNonNull(issuer, "app.security.jwt.issuer é obrigatório");
        Objects.requireNonNull(accessTokenTtl, "app.security.jwt.access-token-ttl é obrigatório");
    }

    public SecretKey secretKey() {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /** Nunca expor o segredo em logs. */
    @Override
    public String toString() {
        return "JwtProperties[secret=***, issuer=" + issuer + ", accessTokenTtl=" + accessTokenTtl + "]";
    }
}
