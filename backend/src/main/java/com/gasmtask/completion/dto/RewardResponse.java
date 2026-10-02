package com.gasmtask.completion.dto;

import com.gasmtask.economy.domain.Reward;

public record RewardResponse(int points, int baseCoins, int onTimeBonus, int proofBonus, int totalCoins) {

    public static RewardResponse of(Reward reward) {
        return new RewardResponse(reward.points(), reward.baseCoins(), reward.onTimeBonus(), reward.proofBonus(),
                reward.totalCoins());
    }
}
