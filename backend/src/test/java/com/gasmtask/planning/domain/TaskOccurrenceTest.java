package com.gasmtask.planning.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import com.gasmtask.economy.domain.Reward;
import com.gasmtask.task.domain.ScheduleSlot;
import com.gasmtask.task.domain.Task;
import com.gasmtask.task.domain.TaskCategory;
import com.gasmtask.task.domain.TaskDefinition;
import com.gasmtask.task.domain.TaskKind;

import org.junit.jupiter.api.Test;

class TaskOccurrenceTest {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate MONDAY = LocalDate.of(2026, 9, 28);
    private static final Instant SUNDAY_NIGHT = Instant.parse("2026-09-28T01:00:00Z"); // domingo 22:00 em SP

    private final UUID userId = UUID.randomUUID();
    private final WeeklyPlan plan = WeeklyPlan.create(userId, MONDAY, SUNDAY_NIGHT);

    private TaskOccurrence occurrence(TaskKind kind, Instant createdAt) {
        Task task = Task.create(userId, new TaskDefinition("Estudar Java", null, TaskCategory.STUDY, kind, 5, 60,
                false, List.of(new ScheduleSlot(MONDAY.getDayOfWeek(), LocalTime.of(8, 0)))), createdAt);
        return TaskOccurrence.fromTask(task, plan, MONDAY, LocalTime.of(8, 0), 3, createdAt);
    }

    @Test
    void guardaORetratoDaMissao() {
        TaskOccurrence occurrence = occurrence(TaskKind.MANDATORY, SUNDAY_NIGHT);

        assertThat(occurrence.getTitle()).isEqualTo("Estudar Java");
        assertThat(occurrence.getPoints()).isEqualTo(5);
        assertThat(occurrence.getBaseCoins()).isEqualTo(3);
        assertThat(occurrence.isPending()).isTrue();
    }

    @Test
    void concluiUmaVezSo() {
        TaskOccurrence occurrence = occurrence(TaskKind.MANDATORY, SUNDAY_NIGHT);

        occurrence.complete(SUNDAY_NIGHT.plusSeconds(36_000), true, new Reward(5, 3, 1, 0));

        assertThat(occurrence.isCompleted()).isTrue();
        assertThat(occurrence.getEarnedCoins()).isEqualTo(4);
        assertThatThrownBy(() -> occurrence.complete(SUNDAY_NIGHT, true, new Reward(5, 3, 1, 0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void perdidaSoAfetaPendente() {
        TaskOccurrence done = occurrence(TaskKind.MANDATORY, SUNDAY_NIGHT);
        done.complete(SUNDAY_NIGHT, false, new Reward(5, 3, 0, 0));

        done.markMissed();

        assertThat(done.getStatus()).isEqualTo(OccurrenceStatus.COMPLETED);
    }

    @Test
    void planejadaAntesDoDiaUsaOFusoDoUsuario() {
        assertThat(occurrence(TaskKind.MANDATORY, SUNDAY_NIGHT).plannedInAdvance(SAO_PAULO)).isTrue();
        // Mesmo instante em UTC já é segunda: no fuso errado pareceria planejada no próprio dia
        assertThat(occurrence(TaskKind.MANDATORY, SUNDAY_NIGHT).plannedInAdvance(ZoneId.of("UTC"))).isFalse();
    }

    @Test
    void progressoDoDiaContaSoObrigatoriasParaCumprir() {
        TaskOccurrence mandatory = occurrence(TaskKind.MANDATORY, SUNDAY_NIGHT);
        TaskOccurrence extra = TaskOccurrence.extra(userId, plan, "Arrumar a mesa", TaskCategory.HOME, 2, 1, MONDAY,
                null, null, false, SUNDAY_NIGHT);
        extra.complete(SUNDAY_NIGHT, false, new Reward(2, 1, 0, 0));

        DayProgress before = DayProgress.of(List.of(mandatory, extra));
        mandatory.complete(SUNDAY_NIGHT, true, new Reward(5, 3, 1, 0));
        DayProgress after = DayProgress.of(List.of(mandatory, extra));

        assertThat(before.fulfilled()).isFalse();
        assertThat(after.fulfilled()).isTrue();
        assertThat(after).isEqualTo(new DayProgress(1, 1, 1, 1, 7, 5));
    }
}
