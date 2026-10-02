package com.gasmtask.achievement.dto;

import java.time.Instant;

import com.gasmtask.achievement.domain.AchievementCriterion;
import com.gasmtask.task.domain.TaskCategory;

/** Conquista com progresso (RF14): {@code progress} de {@code threshold}, e a data quando desbloqueada. */
public record AchievementResponse(String code, String name, String description, AchievementCriterion criterion,
                                  TaskCategory category, int threshold, int progress, boolean unlocked,
                                  Instant unlockedAt, String assetKey) {
}
