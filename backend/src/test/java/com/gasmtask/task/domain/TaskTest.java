package com.gasmtask.task.domain;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class TaskTest {

    private static final Instant NOW = Instant.parse("2026-09-28T12:00:00Z");
    private static final LocalTime EIGHT = LocalTime.of(8, 0);

    private static TaskDefinition definition(String name, List<ScheduleSlot> slots) {
        return new TaskDefinition(name, null, TaskCategory.STUDY, TaskKind.MANDATORY, 5, 60, false, slots);
    }

    @Test
    void edicaoSemMudancaNaoRefazOPlano() {
        List<ScheduleSlot> slots = List.of(new ScheduleSlot(MONDAY, EIGHT), new ScheduleSlot(WEDNESDAY, EIGHT));
        Task task = Task.create(UUID.randomUUID(), definition("Estudar Java", slots), NOW);

        TaskChange change = task.update(definition("Estudar Java", List.of(slots.get(1), slots.get(0))), NOW);

        assertThat(change).isEqualTo(new TaskChange(false, false));
    }

    @Test
    void trocarSoONomeRenomeiaSemRefazerOPlano() {
        List<ScheduleSlot> slots = List.of(new ScheduleSlot(MONDAY, EIGHT));
        Task task = Task.create(UUID.randomUUID(), definition("Estudar Java", slots), NOW);

        TaskChange change = task.update(definition("Estudar Spring", slots), NOW);

        assertThat(change).isEqualTo(new TaskChange(false, true));
        assertThat(task.getName()).isEqualTo("Estudar Spring");
    }

    @Test
    void recorrenciaEAtualizadaNoLugarMantendoOsDiasQueContinuam() {
        Task task = Task.create(UUID.randomUUID(), definition("Estudar Java",
                List.of(new ScheduleSlot(MONDAY, EIGHT), new ScheduleSlot(WEDNESDAY, EIGHT))), NOW);
        TaskSchedule monday = task.getSchedule().stream()
                .filter(entry -> entry.getDayOfWeek() == MONDAY).findFirst().orElseThrow();

        TaskChange change = task.update(definition("Estudar Java",
                List.of(new ScheduleSlot(MONDAY, LocalTime.of(9, 0)), new ScheduleSlot(FRIDAY, null))), NOW);

        assertThat(change.planChanged()).isTrue();
        assertThat(task.slots()).containsExactly(new ScheduleSlot(MONDAY, LocalTime.of(9, 0)), new ScheduleSlot(FRIDAY, null));
        assertThat(task.getSchedule()).contains(monday);
        assertThat(monday.getPlannedTime()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    void diaRepetidoNaRecorrenciaERecusado() {
        List<ScheduleSlot> repeated = List.of(new ScheduleSlot(MONDAY, EIGHT), new ScheduleSlot(MONDAY, null));

        assertThatThrownBy(() -> Task.create(UUID.randomUUID(), definition("Estudar Java", repeated), NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void arquivarEIdempotente() {
        Task task = Task.create(UUID.randomUUID(), definition("Ler", List.of(new ScheduleSlot(MONDAY, null))), NOW);

        task.archive(NOW);
        task.archive(NOW.plusSeconds(60));

        assertThat(task.isArchived()).isTrue();
        assertThat(task.getArchivedAt()).isEqualTo(NOW);
    }
}
