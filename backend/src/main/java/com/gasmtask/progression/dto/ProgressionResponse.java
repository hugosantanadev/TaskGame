package com.gasmtask.progression.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.gasmtask.progression.domain.RankTier;
import com.gasmtask.progression.domain.XpReason;
import com.gasmtask.store.dto.StoreItemResponse;

/**
 * O elo por inteiro: onde a pessoa está, o maior elo que já alcançou, a escada, as roupas de cada elo
 * ({@code item.owned} diz se já foi desbloqueada) e as últimas mudanças de XP.
 */
public record ProgressionResponse(
        RankStatusResponse status,
        int peakXp,
        RankResponse peakRank,
        List<Step> ladder,
        List<Reward> rewards,
        List<Event> recent) {

    public record Step(RankResponse rank, int minXp) {
    }

    public record Reward(RankTier tier, StoreItemResponse item) {
    }

    /** @param title nome da tarefa que rendeu ou custou o XP, quando houver */
    public record Event(int amount, XpReason reason, String title, LocalDate eventDate, Instant createdAt) {
    }
}
