package com.gasmtask.challenge.domain;

import java.time.LocalTime;
import java.util.List;
import java.util.function.Predicate;

/**
 * Catálogo de desafios diários. Cada um sabe se dá para cumprir com o plano do dia (para entrar no sorteio)
 * e quanto já foi feito. Só entram no sorteio desafios ainda não cumpridos: ninguém ganha por algo já feito.
 */
public enum ChallengeType {

    /** Concluir 2 tarefas antes do meio-dia. Só sorteado de manhã cedo, quando ainda dá tempo. */
    EARLY_BIRD(2) {
        @Override
        boolean possible(List<ChallengeTask> tasks, LocalTime now) {
            return now.isBefore(LocalTime.of(10, 0)) && tasks.size() >= target();
        }

        @Override
        public int progress(List<ChallengeTask> tasks) {
            return count(tasks, task -> task.completed() && task.completedAt().isBefore(NOON));
        }
    },

    /** Concluir 2 tarefas no horário (as que podem render bônus de horário). */
    ON_TIME(2) {
        @Override
        boolean possible(List<ChallengeTask> tasks, LocalTime now) {
            return count(tasks, task -> task.pending() && task.timedInAdvance()) >= target();
        }

        @Override
        public int progress(List<ChallengeTask> tasks) {
            return count(tasks, task -> task.completed() && task.onTime());
        }
    },

    /** Concluir 1 tarefa com foto. */
    PHOTO(1) {
        @Override
        boolean possible(List<ChallengeTask> tasks, LocalTime now) {
            return tasks.stream().anyMatch(ChallengeTask::pending);
        }

        @Override
        public int progress(List<ChallengeTask> tasks) {
            return count(tasks, task -> task.completed() && task.proofAttached());
        }
    },

    /** Concluir 1 extra. */
    EXTRA_MILE(1) {
        @Override
        boolean possible(List<ChallengeTask> tasks, LocalTime now) {
            return tasks.stream().anyMatch(task -> task.pending() && task.extra());
        }

        @Override
        public int progress(List<ChallengeTask> tasks) {
            return count(tasks, task -> task.completed() && task.extra());
        }
    },

    /** Concluir tarefas de 3 categorias diferentes. */
    VARIETY(3) {
        @Override
        boolean possible(List<ChallengeTask> tasks, LocalTime now) {
            return tasks.stream().filter(task -> task.pending() || task.completed())
                    .map(ChallengeTask::category).distinct().count() >= target();
        }

        @Override
        public int progress(List<ChallengeTask> tasks) {
            return (int) tasks.stream().filter(ChallengeTask::completed).map(ChallengeTask::category).distinct().count();
        }
    },

    /** Cumprir o dia: todas as obrigatórias concluídas. */
    FULL_DAY(1) {
        @Override
        boolean possible(List<ChallengeTask> tasks, LocalTime now) {
            return tasks.stream().anyMatch(task -> task.mandatory() && task.pending());
        }

        @Override
        public int progress(List<ChallengeTask> tasks) {
            List<ChallengeTask> mandatory = tasks.stream().filter(ChallengeTask::mandatory).toList();
            return !mandatory.isEmpty() && mandatory.stream().allMatch(ChallengeTask::completed) ? 1 : 0;
        }
    },

    /** Concluir 4 tarefas no dia. */
    MARATHON(4) {
        @Override
        boolean possible(List<ChallengeTask> tasks, LocalTime now) {
            return count(tasks, task -> task.pending() || task.completed()) >= target();
        }

        @Override
        public int progress(List<ChallengeTask> tasks) {
            return count(tasks, ChallengeTask::completed);
        }
    };

    private static final LocalTime NOON = LocalTime.NOON;

    private final int target;

    ChallengeType(int target) {
        this.target = target;
    }

    public int target() {
        return target;
    }

    /** Pode entrar no sorteio: o plano permite cumprir e ainda não foi cumprido. */
    public boolean eligible(List<ChallengeTask> tasks, LocalTime now) {
        return possible(tasks, now) && progress(tasks) < target;
    }

    abstract boolean possible(List<ChallengeTask> tasks, LocalTime now);

    /** Quanto já foi feito hoje (pode passar da meta). */
    public abstract int progress(List<ChallengeTask> tasks);

    private static int count(List<ChallengeTask> tasks, Predicate<ChallengeTask> filter) {
        return (int) tasks.stream().filter(filter).count();
    }
}
