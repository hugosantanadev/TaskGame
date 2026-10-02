package com.gasmtask.economy.config;

import java.time.Duration;

import com.gasmtask.economy.domain.RewardPolicy;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Valores de {@code app.rewards}; viram a {@link RewardPolicy}, que não conhece o Spring. */
@ConfigurationProperties("app.rewards")
public record RewardProperties(
        int mandatoryPoints,
        int mandatoryCoins,
        int extraMinPoints,
        int extraMaxPoints,
        int extraCoins,
        int onTimeBonus,
        int proofBonus,
        Duration onTimeTolerance) {

    public RewardPolicy toPolicy() {
        return new RewardPolicy(mandatoryPoints, mandatoryCoins, extraMinPoints, extraMaxPoints, extraCoins,
                onTimeBonus, proofBonus, onTimeTolerance);
    }
}
