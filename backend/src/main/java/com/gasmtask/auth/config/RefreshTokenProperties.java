package com.gasmtask.auth.config;

import java.time.Duration;
import java.util.Objects;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param ttl               validade de cada refresh token (renovada a cada rotação)
 * @param reuseGracePeriod  janela em que reapresentar um token recém-rotacionado não conta como roubo
 *                          (duas abas renovando juntas, retry de rede)
 * @param cookieName        nome do cookie HttpOnly
 * @param cookiePath        caminho do cookie; restrito às rotas de autenticação
 * @param cookieSecure      exige HTTPS; desligue só em desenvolvimento local via HTTP
 * @param cleanupCron       quando remover tokens expirados do banco
 */
@ConfigurationProperties("app.security.refresh-token")
public record RefreshTokenProperties(
        Duration ttl,
        Duration reuseGracePeriod,
        String cookieName,
        String cookiePath,
        boolean cookieSecure,
        String cleanupCron) {

    public RefreshTokenProperties {
        Objects.requireNonNull(ttl, "app.security.refresh-token.ttl é obrigatório");
        Objects.requireNonNull(reuseGracePeriod, "app.security.refresh-token.reuse-grace-period é obrigatório");
        Objects.requireNonNull(cookieName, "app.security.refresh-token.cookie-name é obrigatório");
        Objects.requireNonNull(cookiePath, "app.security.refresh-token.cookie-path é obrigatório");
    }
}
