package com.gasmtask.planning.domain;

import java.util.Collection;

/** Contagem de um dia: base da tela Hoje, do streak e do fechamento. */
public record DayProgress(int mandatoryPlanned, int mandatoryDone, int extrasPlanned, int extrasDone,
                          int points, int coins) {

    public static DayProgress of(Collection<TaskOccurrence> occurrences) {
        int mandatoryPlanned = 0;
        int mandatoryDone = 0;
        int extrasPlanned = 0;
        int extrasDone = 0;
        int points = 0;
        int coins = 0;
        for (TaskOccurrence occurrence : occurrences) {
            boolean done = occurrence.isCompleted();
            if (occurrence.isMandatory()) {
                mandatoryPlanned++;
                mandatoryDone += done ? 1 : 0;
            } else {
                extrasPlanned++;
                extrasDone += done ? 1 : 0;
            }
            if (done) {
                points += occurrence.getEarnedPoints() == null ? 0 : occurrence.getEarnedPoints();
                coins += occurrence.getEarnedCoins() == null ? 0 : occurrence.getEarnedCoins();
            }
        }
        return new DayProgress(mandatoryPlanned, mandatoryDone, extrasPlanned, extrasDone, points, coins);
    }

    public boolean hasMandatory() {
        return mandatoryPlanned > 0;
    }

    /** Cumprido: havia ao menos uma obrigatória e todas foram concluídas (RN22). */
    public boolean fulfilled() {
        return mandatoryPlanned > 0 && mandatoryDone >= mandatoryPlanned;
    }
}
