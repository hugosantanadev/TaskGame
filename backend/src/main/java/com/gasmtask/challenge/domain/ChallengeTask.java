package com.gasmtask.challenge.domain;

import java.time.LocalTime;

import com.gasmtask.task.domain.TaskCategory;
import com.gasmtask.task.domain.TaskKind;

/**
 * O que os desafios precisam saber de uma tarefa do dia, sem depender da entidade.
 *
 * @param completedAt    hora da conclusão no fuso do usuário; nula se não concluída
 * @param timedInAdvance tem horário e foi planejada antes do dia: é a única que pode render bônus de horário (RN15)
 */
public record ChallengeTask(TaskKind kind, TaskCategory category, boolean pending, LocalTime completedAt,
                            boolean onTime, boolean proofAttached, boolean timedInAdvance) {

    public boolean completed() {
        return completedAt != null;
    }

    public boolean extra() {
        return kind == TaskKind.EXTRA;
    }

    public boolean mandatory() {
        return kind == TaskKind.MANDATORY;
    }
}
