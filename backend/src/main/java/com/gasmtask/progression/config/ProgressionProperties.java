package com.gasmtask.progression.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Quanto o XP muda além dos pontos das tarefas: o bônus de dia cumprido e a perda por obrigatória perdida.
 */
@ConfigurationProperties("app.progression")
public record ProgressionProperties(int dayFulfilledBonus, int missedMandatoryPenalty) {

    public ProgressionProperties {
        if (dayFulfilledBonus < 0 || missedMandatoryPenalty < 0) {
            throw new IllegalArgumentException("Os valores de app.progression não podem ser negativos");
        }
    }
}
