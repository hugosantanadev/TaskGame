package com.gasmtask.user.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Substitui todas as preferências de lembrete (PUT).
 *
 * @param bedtime  hora de dormir; nula desliga o lembrete de dormir
 * @param wakeTime hora de acordar; nula desliga o lembrete de acordar
 */
public record ReminderSettingsRequest(
        @NotNull(message = "diga se quer lembretes das tarefas")
        Boolean tasksEnabled,

        @NotNull(message = "informe a antecedência")
        @Min(value = 0, message = "a antecedência vai de 0 a 120 minutos")
        @Max(value = 120, message = "a antecedência vai de 0 a 120 minutos")
        Integer leadMinutes,

        LocalTime bedtime,

        LocalTime wakeTime) {
}
