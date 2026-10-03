package com.gasmtask.streak.domain;

import java.time.LocalDate;

/**
 * Streak como o usuário vê: dias fechados mais o dia de hoje, se já cumprido.
 *
 * @param freezes        protetores de sequência guardados
 * @param lastFrozenDate último dia salvo por um protetor
 */
public record StreakView(int current, int longest, TodayStatus todayStatus, LocalDate lastFulfilledDate, int freezes,
                         LocalDate lastFrozenDate) {
}
