package com.gasmtask.task.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

/** Um dia da recorrência; {@code time} nulo significa "sem horário". */
public record ScheduleEntry(@NotNull DayOfWeek dayOfWeek, LocalTime time) {
}
