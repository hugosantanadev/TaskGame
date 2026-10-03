package com.gasmtask.stats.domain;

import java.time.LocalDate;

import com.gasmtask.streak.domain.StreakRules;

/**
 * Situação de um dia no resumo semanal. Dias passados já fecharam (cumprido, falha, descanso ou protegido,
 * RN22); hoje ainda pode virar cumprido; dias futuros ainda não começaram.
 */
public enum SummaryDayStatus {
    FULFILLED, FAILED, REST, FROZEN, PENDING, UPCOMING;

    public static SummaryDayStatus of(LocalDate date, LocalDate today, int mandatoryPlanned, int mandatoryDone) {
        if (date.isAfter(today)) {
            return UPCOMING;
        }
        return switch (StreakRules.statusOf(mandatoryPlanned, mandatoryDone)) {
            case FULFILLED -> FULFILLED;
            case REST -> REST;
            case FROZEN -> FROZEN;
            case FAILED -> date.isBefore(today) ? FAILED : PENDING;
        };
    }
}
