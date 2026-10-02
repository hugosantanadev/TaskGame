package com.gasmtask.task.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Um dia da recorrência de uma missão. Único por (missão, dia da semana). */
@Entity
@Table(name = "task_schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskSchedule {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false, updatable = false)
    private Task task;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 9, updatable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "planned_time")
    private LocalTime plannedTime;

    static TaskSchedule of(Task task, ScheduleSlot slot) {
        TaskSchedule schedule = new TaskSchedule();
        schedule.id = UUID.randomUUID();
        schedule.task = task;
        schedule.dayOfWeek = slot.day();
        schedule.plannedTime = slot.time();
        return schedule;
    }

    /** Muda o horário mantendo a linha: trocar a linha inteira violaria a restrição única antes do DELETE. */
    boolean changeTime(LocalTime time) {
        if (java.util.Objects.equals(plannedTime, time)) {
            return false;
        }
        plannedTime = time;
        return true;
    }

    public ScheduleSlot toSlot() {
        return new ScheduleSlot(dayOfWeek, plannedTime);
    }
}
