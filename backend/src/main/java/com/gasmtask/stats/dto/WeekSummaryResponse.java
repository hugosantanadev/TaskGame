package com.gasmtask.stats.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import com.gasmtask.achievement.dto.UnlockedAchievementResponse;
import com.gasmtask.stats.domain.SummaryDayStatus;

/**
 * Resumo de uma semana (RF22): parcial enquanto ela corre, final depois do domingo.
 *
 * @param finished      a semana já terminou; os números não mudam mais
 * @param fulfilledDays dias cumpridos, contando hoje se já estiver cumprido
 * @param achievements  conquistas desbloqueadas na semana, no fuso do usuário
 */
public record WeekSummaryResponse(
        LocalDate weekStart,
        LocalDate weekEnd,
        boolean finished,
        TotalsResponse totals,
        int fulfilledDays,
        List<Day> days,
        List<UnlockedAchievementResponse> achievements) {

    public record Day(LocalDate date, DayOfWeek dayOfWeek, SummaryDayStatus status, TotalsResponse totals) {
    }
}
