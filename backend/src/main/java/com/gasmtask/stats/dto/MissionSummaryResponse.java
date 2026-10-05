package com.gasmtask.stats.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.gasmtask.task.domain.TaskCategory;

/**
 * Uma missão vista de longe: quanto foi feita e a sequência dela.
 *
 * @param completionRate concluídas sobre concluídas + perdidas, em porcentagem inteira; nulo sem nada decidido
 */
public record MissionSummaryResponse(UUID taskId, String name, TaskCategory category, boolean archived,
                                     int completed, int missed, Integer completionRate, int currentStreak,
                                     int bestStreak, LocalDate lastCompletedDate) {
}
