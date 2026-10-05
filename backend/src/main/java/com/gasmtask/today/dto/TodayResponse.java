package com.gasmtask.today.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.gasmtask.challenge.dto.DailyChallengeResponse;
import com.gasmtask.chest.dto.ChestResponse;
import com.gasmtask.planning.dto.OccurrenceResponse;
import com.gasmtask.progression.dto.RankStatusResponse;
import com.gasmtask.shared.time.TimeOfDay;
import com.gasmtask.streak.dto.StreakResponse;

/**
 * Tela Hoje em uma chamada.
 *
 * @param nextOccurrenceId próxima tarefa pendente (a de horário mais próximo ainda dentro da janela)
 * @param onboarding       primeiro dia, antes da primeira conclusão: hoje está totalmente editável
 * @param pendingChest     baú semanal ainda fechado; nulo se não houver
 */
public record TodayResponse(
        LocalDate date,
        TimeOfDay timeOfDay,
        List<OccurrenceResponse> occurrences,
        UUID nextOccurrenceId,
        ProgressResponse progress,
        int walletBalance,
        StreakResponse streak,
        boolean onboarding,
        RankStatusResponse rank,
        List<DailyChallengeResponse> challenges,
        ChestResponse pendingChest) {
}
