package com.gasmtask.economy.domain;

/** Recompensa de uma conclusão, já detalhada para o feedback na tela (+3 base, +1 no horário, +1 prova). */
public record Reward(int points, int baseCoins, int onTimeBonus, int proofBonus) {

    public int totalCoins() {
        return baseCoins + onTimeBonus + proofBonus;
    }
}
