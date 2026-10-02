package com.gasmtask.planning.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

/** Novo dia e horário da tarefa ({@code time} nulo deixa sem horário). */
public record MoveOccurrenceRequest(@NotNull LocalDate date, LocalTime time) {
}
