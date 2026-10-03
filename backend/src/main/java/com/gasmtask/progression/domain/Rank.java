package com.gasmtask.progression.domain;

/**
 * Um degrau da escada: elo, divisão (1 a 3; nula em Lenda) e o XP mínimo para estar nele.
 */
public record Rank(RankTier tier, Integer division, int minXp) {

    public boolean isAbove(Rank other) {
        return minXp > other.minXp;
    }
}
