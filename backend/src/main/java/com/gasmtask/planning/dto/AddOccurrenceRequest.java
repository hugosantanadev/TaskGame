package com.gasmtask.planning.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/** Inclui uma missão num dia. Sem {@code time}, usa o horário da recorrência daquele dia, se houver. */
public record AddOccurrenceRequest(@NotNull UUID taskId, @NotNull LocalDate date, LocalTime time) {
}
