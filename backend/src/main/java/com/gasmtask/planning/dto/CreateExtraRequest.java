package com.gasmtask.planning.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.gasmtask.task.domain.TaskCategory;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Extra avulsa para um dia futuro (RN11): vale 1 ou 2 pontos e nunca afeta o streak. */
public record CreateExtraRequest(
        @NotBlank @Size(max = 60) String title,
        @NotNull TaskCategory category,
        @NotNull @Min(1) @Max(2) Integer points,
        @NotNull LocalDate date,
        LocalTime time,
        @Min(1) @Max(720) Integer durationMinutes,
        boolean requiresProof) {
}
