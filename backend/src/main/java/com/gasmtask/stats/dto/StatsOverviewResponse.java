package com.gasmtask.stats.dto;

import java.util.List;

import com.gasmtask.task.domain.TaskCategory;

/**
 * Totais desde o cadastro.
 *
 * @param completionRate concluídas sobre concluídas + perdidas (as pendentes ainda não contam), em porcentagem
 *                       inteira; nulo enquanto nenhuma tarefa foi decidida
 * @param fulfilledDays  dias já fechados como cumpridos (o de hoje entra só depois da virada)
 * @param frozenDays     dias de falha salvos pelo protetor de sequência
 */
public record StatsOverviewResponse(
        int completedTasks,
        int missedTasks,
        Integer completionRate,
        int points,
        int coinsEarned,
        int coinsSpent,
        int balance,
        int currentStreak,
        int longestStreak,
        int fulfilledDays,
        int failedDays,
        int restDays,
        int frozenDays,
        int achievementsUnlocked,
        List<CategoryTotal> completedByCategory) {

    public record CategoryTotal(TaskCategory category, int completed) {
    }
}
