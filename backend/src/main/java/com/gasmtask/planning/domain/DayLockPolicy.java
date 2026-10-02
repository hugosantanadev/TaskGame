package com.gasmtask.planning.domain;

import java.time.LocalDate;

import com.gasmtask.task.domain.TaskKind;

/**
 * Dia congelado (RN02, revisada na Fase 2): o passado nunca muda; hoje aceita inclusões, mas não remoções.
 * Tirar uma obrigatória de hoje (remover, mover ou mudar o horário) é o que permitiria salvar o streak
 * apagando às 23h a tarefa não feita. Incluir só deixa o dia mais difícil.
 * Extras continuam só a partir de amanhã (RN11).
 * No dia do cadastro, até a primeira conclusão, hoje é totalmente editável (RN03).
 */
public final class DayLockPolicy {

    private DayLockPolicy() {
    }

    /** Pode colocar uma tarefa desse tipo nesse dia (criar, incluir ou mover para ele)? */
    public static boolean canPlace(TaskKind kind, LocalDate date, LocalDate today, boolean onboardingOpen) {
        if (date.isBefore(today)) {
            return false;
        }
        if (date.isAfter(today)) {
            return true;
        }
        return kind == TaskKind.MANDATORY || onboardingOpen;
    }

    /** Pode tirar uma tarefa pendente desse dia (remover, mover para outro dia ou mudar o horário)? */
    public static boolean canTakeOut(LocalDate date, LocalDate today, boolean onboardingOpen) {
        return date.isAfter(today) || (date.isEqual(today) && onboardingOpen);
    }
}
