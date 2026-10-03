package com.gasmtask.completion.dto;

import java.util.List;

import com.gasmtask.achievement.dto.UnlockedAchievementResponse;
import com.gasmtask.challenge.dto.DailyChallengeResponse;
import com.gasmtask.character.dto.AttributeGainResponse;
import com.gasmtask.progression.dto.XpChangeResponse;
import com.gasmtask.planning.dto.OccurrenceResponse;
import com.gasmtask.streak.domain.TodayStatus;

/**
 * Resultado de uma conclusão, com tudo o que a tela precisa para o feedback: a recompensa detalhada,
 * o novo saldo, o progresso do dia, se o streak acabou de subir e as conquistas desbloqueadas agora.
 */
public record CompletionResponse(
        OccurrenceResponse occurrence,
        boolean onTime,
        RewardResponse reward,
        int walletBalance,
        Day day,
        StreakChange streak,
        List<UnlockedAchievementResponse> unlockedAchievements,
        XpChangeResponse xp,
        AttributeGainResponse attribute,
        List<DailyChallengeResponse> completedChallenges) {

    public record Day(TodayStatus status, int mandatoryDone, int mandatoryPlanned) {
    }

    public record StreakChange(int current, int longest, boolean increasedNow) {
    }
}
