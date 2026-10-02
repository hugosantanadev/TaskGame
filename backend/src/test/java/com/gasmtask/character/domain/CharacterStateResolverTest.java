package com.gasmtask.character.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.gasmtask.economy.domain.Reward;
import com.gasmtask.planning.domain.TaskOccurrence;
import com.gasmtask.planning.domain.WeeklyPlan;
import com.gasmtask.task.domain.TaskCategory;

import org.junit.jupiter.api.Test;

class CharacterStateResolverTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 9, 28);
    private static final Instant PLANNED = Instant.parse("2026-09-27T12:00:00Z");

    private final UUID userId = UUID.randomUUID();
    private final WeeklyPlan plan = WeeklyPlan.create(userId, MONDAY, PLANNED);

    private TaskOccurrence task(TaskCategory category, String time, Integer durationMinutes) {
        return TaskOccurrence.extra(userId, plan, "Tarefa", category, 1, 1, MONDAY, LocalTime.parse(time),
                durationMinutes, false, PLANNED);
    }

    private static CharacterState at(List<TaskOccurrence> tasks, String time) {
        return CharacterStateResolver.resolve(tasks, LocalTime.parse(time));
    }

    @Test
    void categoriaDefineAAtividade() {
        assertThat(CharacterStateResolver.stateFor(TaskCategory.STUDY)).isEqualTo(CharacterState.STUDYING);
        assertThat(CharacterStateResolver.stateFor(TaskCategory.PROJECT)).isEqualTo(CharacterState.AT_COMPUTER);
        assertThat(CharacterStateResolver.stateFor(TaskCategory.READING)).isEqualTo(CharacterState.READING);
        assertThat(CharacterStateResolver.stateFor(TaskCategory.SPIRITUALITY)).isEqualTo(CharacterState.READING);
        assertThat(CharacterStateResolver.stateFor(TaskCategory.SLEEP)).isEqualTo(CharacterState.SLEEPING);
        assertThat(CharacterStateResolver.stateFor(TaskCategory.EXERCISE)).isEqualTo(CharacterState.IDLE);
    }

    @Test
    void tarefaEmAndamentoDefineOEstadoDoInicioAteOFim() {
        List<TaskOccurrence> tasks = List.of(task(TaskCategory.READING, "20:00", 60));

        assertThat(at(tasks, "19:59")).isEqualTo(CharacterState.IDLE);
        assertThat(at(tasks, "20:00")).isEqualTo(CharacterState.READING);
        assertThat(at(tasks, "20:59")).isEqualTo(CharacterState.READING);
        assertThat(at(tasks, "21:00")).isEqualTo(CharacterState.IDLE);
    }

    @Test
    void semDuracaoValeMeiaHora() {
        List<TaskOccurrence> tasks = List.of(task(TaskCategory.STUDY, "08:00", null));

        assertThat(at(tasks, "08:29")).isEqualTo(CharacterState.STUDYING);
        assertThat(at(tasks, "08:30")).isEqualTo(CharacterState.IDLE);
    }

    @Test
    void concluirEncerraAAtividade() {
        TaskOccurrence study = task(TaskCategory.STUDY, "08:00", 60);
        study.complete(PLANNED, false, new Reward(1, 1, 0, 0));

        assertThat(at(List.of(study), "08:10")).isEqualTo(CharacterState.IDLE);
    }

    @Test
    void venceATarefaQueComecouPorUltimo() {
        List<TaskOccurrence> tasks = List.of(task(TaskCategory.STUDY, "08:00", 120), task(TaskCategory.PROJECT, "09:00", 60));

        assertThat(at(tasks, "08:30")).isEqualTo(CharacterState.STUDYING);
        assertThat(at(tasks, "09:15")).isEqualTo(CharacterState.AT_COMPUTER);
    }
}
