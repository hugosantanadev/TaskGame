package com.gasmtask.task.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.gasmtask.task.domain.TaskCategory;
import com.gasmtask.task.domain.TaskKind;

public record TaskResponse(
        UUID id,
        String name,
        String description,
        TaskCategory category,
        TaskKind kind,
        int points,
        int coins,
        Integer durationMinutes,
        boolean requiresProof,
        List<ScheduleEntry> schedule,
        boolean archived,
        Instant createdAt) {
}
