package com.gasmtask.task.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

/** Um dia da recorrência de uma missão, com horário opcional (RN07). */
public record ScheduleSlot(DayOfWeek day, LocalTime time) {

    public ScheduleSlot {
        Objects.requireNonNull(day, "day");
    }
}
