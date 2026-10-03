package com.gasmtask.progression.domain;

/**
 * Os elos, do mais baixo ao mais alto. Todos têm três divisões, menos Lenda, que é o topo.
 * Cada elo a partir do Bronze libera uma roupa exclusiva ({@code rewardCode}, item fora da loja).
 */
public enum RankTier {
    IRON(null),
    BRONZE("rank_bronze_headband"),
    SILVER("rank_silver_jacket"),
    GOLD("rank_gold_chain"),
    PLATINUM("rank_platinum_visor"),
    DIAMOND("rank_diamond_armor"),
    MASTER("rank_master_cape"),
    LEGEND("rank_legend_crown");

    private final String rewardCode;

    RankTier(String rewardCode) {
        this.rewardCode = rewardCode;
    }

    /** Código do item ganho ao chegar neste elo; nulo no Ferro, que é onde todo mundo começa. */
    public String rewardCode() {
        return rewardCode;
    }

    public boolean hasDivisions() {
        return this != LEGEND;
    }
}
