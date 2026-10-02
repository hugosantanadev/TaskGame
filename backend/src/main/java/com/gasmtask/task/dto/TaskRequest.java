package com.gasmtask.task.dto;

import java.util.List;

import com.gasmtask.task.domain.TaskCategory;
import com.gasmtask.task.domain.TaskKind;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Criação e edição de missão.
 *
 * @param points     só para extras (1 ou 2); obrigatórias valem sempre o padrão da política de recompensas
 * @param startToday na criação, inclui a ocorrência de hoje quando o dia está na recorrência; ignorado na edição
 */
public record TaskRequest(
        @NotBlank @Size(max = 60) String name,
        @Size(max = 280) String description,
        @NotNull TaskCategory category,
        @NotNull TaskKind kind,
        @Min(1) @Max(2) Integer points,
        @Min(1) @Max(720) Integer durationMinutes,
        boolean requiresProof,
        @NotEmpty @Size(max = 7) List<@Valid @NotNull ScheduleEntry> schedule,
        boolean startToday) {
}
