package com.gasmtask.stats.domain;

import java.util.List;

import com.gasmtask.planning.domain.OccurrenceStatus;

/**
 * Sequência de uma missão, pura: quantas vezes seguidas ela foi cumprida, contando só os dias em que estava no
 * plano. Pendentes (hoje e o futuro) não contam nem quebram; uma perdida quebra.
 *
 * @param current vezes seguidas até a última decidida
 * @param best    maior sequência já feita
 */
public record MissionStreaks(int current, int best) {

    /** @param statuses situação de cada ocorrência da missão, em ordem de data */
    public static MissionStreaks of(List<OccurrenceStatus> statuses) {
        int run = 0;
        int best = 0;
        for (OccurrenceStatus status : statuses) {
            if (status == OccurrenceStatus.COMPLETED) {
                run++;
                best = Math.max(best, run);
            } else if (status == OccurrenceStatus.MISSED) {
                run = 0;
            }
        }
        return new MissionStreaks(run, best);
    }
}
