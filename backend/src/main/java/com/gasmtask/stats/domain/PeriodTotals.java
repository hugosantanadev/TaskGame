package com.gasmtask.stats.domain;

import com.gasmtask.planning.domain.OccurrenceStatus;
import com.gasmtask.task.domain.TaskKind;

/**
 * Somatório de um período (dia, semana ou mês): planejado × concluído, perdidas, pontos e moedas obtidos.
 * Imutável; cada {@code plus} devolve um novo total.
 */
public record PeriodTotals(int mandatoryPlanned, int mandatoryDone, int extrasPlanned, int extrasDone, int missed,
                           int points, int coins) {

    public static final PeriodTotals EMPTY = new PeriodTotals(0, 0, 0, 0, 0, 0, 0);

    /** Soma {@code count} tarefas do mesmo tipo e situação, com os pontos e moedas que elas renderam. */
    public PeriodTotals plus(TaskKind kind, OccurrenceStatus status, int count, int earnedPoints, int earnedCoins) {
        boolean mandatory = kind == TaskKind.MANDATORY;
        boolean done = status == OccurrenceStatus.COMPLETED;
        return new PeriodTotals(
                mandatoryPlanned + (mandatory ? count : 0),
                mandatoryDone + (mandatory && done ? count : 0),
                extrasPlanned + (mandatory ? 0 : count),
                extrasDone + (!mandatory && done ? count : 0),
                missed + (status == OccurrenceStatus.MISSED ? count : 0),
                points + earnedPoints,
                coins + earnedCoins);
    }

    public PeriodTotals plus(PeriodTotals other) {
        return new PeriodTotals(mandatoryPlanned + other.mandatoryPlanned, mandatoryDone + other.mandatoryDone,
                extrasPlanned + other.extrasPlanned, extrasDone + other.extrasDone, missed + other.missed,
                points + other.points, coins + other.coins);
    }

    public int planned() {
        return mandatoryPlanned + extrasPlanned;
    }

    public int done() {
        return mandatoryDone + extrasDone;
    }

    /**
     * Concluídas sobre planejadas, em porcentagem arredondada para baixo (100 só quando tudo foi feito).
     * Nulo quando nada foi planejado: "0%" num período vazio daria a impressão de fracasso.
     */
    public Integer completionRate() {
        return planned() == 0 ? null : done() * 100 / planned();
    }
}
