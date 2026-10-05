package com.gasmtask.stats.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * A evolução de uma missão semana a semana (ex.: quantas vezes foi à academia em cada semana).
 *
 * @param weeks         das mais antigas à atual, no mesmo formato do histórico geral
 * @param bestMonth     mês com mais conclusões (primeiro dia do mês); nulo sem nenhuma
 * @param weeklyAverage média de conclusões por semana no período mostrado, com uma casa decimal
 */
public record MissionEvolutionResponse(MissionSummaryResponse summary, List<StatsHistoryResponse.Period> weeks,
                                       LocalDate bestMonth, int bestMonthCompleted, double weeklyAverage,
                                       LocalDate firstCompletedDate) {
}
