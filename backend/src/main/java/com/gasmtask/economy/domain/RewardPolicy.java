package com.gasmtask.economy.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import com.gasmtask.task.domain.TaskKind;

/**
 * Regras de recompensa (RN15, RN17), sem dependência de framework. Os valores vêm da configuração
 * {@code app.rewards}; o usuário nunca digita pontos livres, porque o ranking ficaria inflável.
 */
public record RewardPolicy(
        int mandatoryPoints,
        int mandatoryCoins,
        int extraMinPoints,
        int extraMaxPoints,
        int extraCoins,
        int onTimeBonus,
        int proofBonus,
        Duration onTimeTolerance) {

    public RewardPolicy {
        if (mandatoryPoints < 1 || mandatoryCoins < 0 || extraCoins < 0 || onTimeBonus < 0 || proofBonus < 0) {
            throw new IllegalArgumentException("Valores de recompensa inválidos");
        }
        if (extraMinPoints < 1 || extraMaxPoints < extraMinPoints) {
            throw new IllegalArgumentException("Faixa de pontos de extra inválida");
        }
        if (onTimeTolerance == null || onTimeTolerance.isNegative()) {
            throw new IllegalArgumentException("Tolerância de pontualidade inválida");
        }
    }

    public int baseCoinsFor(TaskKind kind) {
        return kind == TaskKind.MANDATORY ? mandatoryCoins : extraCoins;
    }

    public boolean acceptsExtraPoints(int points) {
        return points >= extraMinPoints && points <= extraMaxPoints;
    }

    /**
     * RN15: no horário é concluir entre (horário − tolerância) e (horário + duração + tolerância).
     * Sem horário não há bônus. O bônus também exige que a tarefa tenha sido planejada antes do dia:
     * senão bastaria incluir uma tarefa "para agora" e concluí-la em seguida.
     */
    public boolean isOnTime(LocalDate date, LocalTime plannedTime, Integer durationMinutes,
                            boolean plannedInAdvance, ZonedDateTime completedAt) {
        if (plannedTime == null || !plannedInAdvance) {
            return false;
        }
        ZonedDateTime planned = ZonedDateTime.of(date, plannedTime, completedAt.getZone());
        ZonedDateTime windowStart = planned.minus(onTimeTolerance);
        ZonedDateTime windowEnd = planned.plusMinutes(durationMinutes == null ? 0 : durationMinutes).plus(onTimeTolerance);
        return !completedAt.isBefore(windowStart) && !completedAt.isAfter(windowEnd);
    }

    public Reward rewardFor(int points, int baseCoins, boolean onTime, boolean withProof) {
        return new Reward(points, baseCoins, onTime ? onTimeBonus : 0, withProof ? proofBonus : 0);
    }
}
