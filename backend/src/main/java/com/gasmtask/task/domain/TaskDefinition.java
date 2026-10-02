package com.gasmtask.task.domain;

import java.util.List;

/** Dados de uma missão já validados e normalizados, prontos para criar ou atualizar a entidade. */
public record TaskDefinition(
        String name,
        String description,
        TaskCategory category,
        TaskKind kind,
        int points,
        Integer durationMinutes,
        boolean requiresProof,
        List<ScheduleSlot> slots) {

    public TaskDefinition {
        slots = List.copyOf(slots);
    }
}
