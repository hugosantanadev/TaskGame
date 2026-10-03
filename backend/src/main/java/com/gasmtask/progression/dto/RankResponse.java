package com.gasmtask.progression.dto;

import com.gasmtask.progression.domain.Rank;
import com.gasmtask.progression.domain.RankTier;

/** @param division 1 a 3; nula em Lenda */
public record RankResponse(RankTier tier, Integer division) {

    public static RankResponse of(Rank rank) {
        return new RankResponse(rank.tier(), rank.division());
    }
}
