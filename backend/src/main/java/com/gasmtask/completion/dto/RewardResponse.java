package com.gasmtask.completion.dto;

import com.gasmtask.economy.domain.Reward;

/** @param equipmentBonus moedas a mais das melhorias do quarto na categoria da tarefa */
public record RewardResponse(int points, int baseCoins, int onTimeBonus, int proofBonus, int equipmentBonus,
                             int totalCoins) {

    public static RewardResponse of(Reward reward) {
        return new RewardResponse(reward.points(), reward.baseCoins(), reward.onTimeBonus(), reward.proofBonus(),
                reward.equipmentBonus(), reward.totalCoins());
    }
}
