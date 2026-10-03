package com.gasmtask.progression.dto;

import com.gasmtask.progression.domain.Rank;
import com.gasmtask.progression.domain.RankLadder;
import com.gasmtask.progression.domain.RankTier;

/**
 * Elo atual e quanto falta para o próximo: a barra vai de {@code rankStartXp} a {@code nextRankXp}.
 *
 * @param nextRankXp XP do próximo degrau; nulo em Lenda
 */
public record RankStatusResponse(RankTier tier, Integer division, int xp, int rankStartXp, Integer nextRankXp) {

    public static RankStatusResponse of(int xp) {
        Rank rank = RankLadder.rankOf(xp);
        return new RankStatusResponse(rank.tier(), rank.division(), xp, rank.minXp(),
                RankLadder.nextAfter(rank).map(Rank::minXp).orElse(null));
    }
}
