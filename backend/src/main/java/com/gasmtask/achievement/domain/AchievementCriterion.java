package com.gasmtask.achievement.domain;

import com.gasmtask.task.domain.TaskCategory;

/**
 * Critérios de conquista do MVP (RN27), no padrão strategy: cada critério sabe medir o próprio progresso.
 * Um critério novo é uma constante nova aqui e uma linha no seed; o serviço não muda.
 */
public enum AchievementCriterion {

    TOTAL_COMPLETIONS {
        @Override
        public int progressOf(AchievementMetrics metrics, TaskCategory category) {
            return metrics.totalCompletions();
        }
    },
    CATEGORY_COMPLETIONS {
        @Override
        public int progressOf(AchievementMetrics metrics, TaskCategory category) {
            return metrics.completionsIn(category);
        }
    },
    SAME_TASK_COMPLETIONS {
        @Override
        public int progressOf(AchievementMetrics metrics, TaskCategory category) {
            return metrics.maxSameTaskCompletions();
        }
    },
    STREAK_REACHED {
        @Override
        public int progressOf(AchievementMetrics metrics, TaskCategory category) {
            return metrics.longestStreak();
        }
    },
    WEEK_COMPLETE {
        @Override
        public int progressOf(AchievementMetrics metrics, TaskCategory category) {
            return metrics.completeWeeks();
        }
    };

    public abstract int progressOf(AchievementMetrics metrics, TaskCategory category);
}
