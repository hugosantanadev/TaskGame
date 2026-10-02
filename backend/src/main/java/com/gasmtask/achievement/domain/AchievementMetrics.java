package com.gasmtask.achievement.domain;

import java.util.Map;

import com.gasmtask.task.domain.TaskCategory;

/** Números do usuário que os critérios de conquista consultam, calculados uma vez por avaliação. */
public record AchievementMetrics(int totalCompletions, Map<TaskCategory, Integer> completionsByCategory,
                                 int maxSameTaskCompletions, int longestStreak, int completeWeeks) {

    public AchievementMetrics {
        completionsByCategory = Map.copyOf(completionsByCategory);
    }

    public int completionsIn(TaskCategory category) {
        return category == null ? 0 : completionsByCategory.getOrDefault(category, 0);
    }
}
