package com.gasmtask.planning.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.gasmtask.planning.domain.OccurrenceStatus;
import com.gasmtask.task.domain.TaskCategory;
import com.gasmtask.task.domain.TaskKind;

/**
 * Uma tarefa num dia.
 *
 * @param coins     moedas base da tarefa (sem bônus)
 * @param removable se pode ser removida, movida ou ter o horário trocado agora (regra do dia congelado)
 */
public record OccurrenceResponse(
        UUID id,
        UUID taskId,
        LocalDate date,
        LocalTime plannedTime,
        String title,
        TaskCategory category,
        TaskKind kind,
        int points,
        int coins,
        Integer durationMinutes,
        boolean requiresProof,
        OccurrenceStatus status,
        Instant completedAt,
        Boolean onTime,
        Integer earnedPoints,
        Integer earnedCoins,
        boolean proofAttached,
        boolean removable) {
}
