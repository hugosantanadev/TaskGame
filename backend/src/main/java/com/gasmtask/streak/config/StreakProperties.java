package com.gasmtask.streak.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Protetor de sequência: quanto custa e quantos dá para guardar. */
@ConfigurationProperties("app.streak")
public record StreakProperties(int freezePrice, int maxFreezes) {

    public StreakProperties {
        if (freezePrice < 1 || maxFreezes < 1) {
            throw new IllegalArgumentException("Valores inválidos em app.streak");
        }
    }
}
