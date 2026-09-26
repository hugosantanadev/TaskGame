package com.gasmtask.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class JwtPropertiesTest {

    @Test
    void refusesSecretsShorterThan32Bytes() {
        assertThatThrownBy(() -> new JwtProperties("curto-demais", "gasmtask", Duration.ofMinutes(15)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    void neverPrintsTheSecret() {
        String secret = "segredo-que-nunca-deve-aparecer-em-log";
        JwtProperties properties = new JwtProperties(secret, "gasmtask", Duration.ofMinutes(15));

        assertThat(properties.toString()).doesNotContain(secret).contains("***");
    }
}
