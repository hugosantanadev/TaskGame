package com.gasmtask.challenge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Quantos desafios por dia e quanto cada um paga. */
@ConfigurationProperties("app.challenges")
public record ChallengeProperties(int perDay, int xpReward, int coinReward) {

    public ChallengeProperties {
        if (perDay < 1 || xpReward < 0 || coinReward < 0) {
            throw new IllegalArgumentException("Valores inválidos em app.challenges");
        }
    }
}
