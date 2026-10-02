package com.gasmtask.character.domain;

import java.time.LocalTime;
import java.util.Collection;
import java.util.Comparator;

import com.gasmtask.planning.domain.TaskOccurrence;
import com.gasmtask.task.domain.TaskCategory;

/**
 * RN05: o estado do personagem vem da tarefa em andamento. Em andamento é uma tarefa pendente de hoje
 * cujo horário já começou e ainda não terminou (sem duração, vale uma janela de 30 minutos).
 * Se houver mais de uma, vence a que começou por último. Concluir a tarefa encerra a atividade.
 */
public final class CharacterStateResolver {

    static final int DEFAULT_DURATION_MINUTES = 30;

    private CharacterStateResolver() {
    }

    public static CharacterState resolve(Collection<TaskOccurrence> today, LocalTime now) {
        int nowMinutes = minutesOf(now);
        return today.stream()
                .filter(TaskOccurrence::isPending)
                .filter(occurrence -> occurrence.getPlannedTime() != null)
                .filter(occurrence -> {
                    int start = minutesOf(occurrence.getPlannedTime());
                    int duration = occurrence.getDurationMinutes() == null
                            ? DEFAULT_DURATION_MINUTES
                            : occurrence.getDurationMinutes();
                    return nowMinutes >= start && nowMinutes < start + duration;
                })
                .max(Comparator.comparing(TaskOccurrence::getPlannedTime))
                .map(occurrence -> stateFor(occurrence.getCategory()))
                .orElse(CharacterState.IDLE);
    }

    public static CharacterState stateFor(TaskCategory category) {
        return switch (category) {
            case STUDY -> CharacterState.STUDYING;
            case PROJECT -> CharacterState.AT_COMPUTER;
            case READING, SPIRITUALITY -> CharacterState.READING;
            case SLEEP -> CharacterState.SLEEPING;
            case EXERCISE, HOME, OTHER -> CharacterState.IDLE;
        };
    }

    private static int minutesOf(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }
}
