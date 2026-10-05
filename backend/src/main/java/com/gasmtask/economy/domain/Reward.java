package com.gasmtask.economy.domain;

/**
 * Recompensa de uma conclusão, já detalhada para o feedback na tela (+3 base, +1 no horário, +1 prova e o bônus
 * das melhorias do quarto na categoria da tarefa).
 */
public record Reward(int points, int baseCoins, int onTimeBonus, int proofBonus, int equipmentBonus) {

    public int totalCoins() {
        return baseCoins + onTimeBonus + proofBonus + equipmentBonus;
    }
}
