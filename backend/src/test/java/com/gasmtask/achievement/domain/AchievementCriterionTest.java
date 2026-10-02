package com.gasmtask.achievement.domain;

import static com.gasmtask.achievement.domain.AchievementCriterion.CATEGORY_COMPLETIONS;
import static com.gasmtask.achievement.domain.AchievementCriterion.SAME_TASK_COMPLETIONS;
import static com.gasmtask.achievement.domain.AchievementCriterion.STREAK_REACHED;
import static com.gasmtask.achievement.domain.AchievementCriterion.TOTAL_COMPLETIONS;
import static com.gasmtask.achievement.domain.AchievementCriterion.WEEK_COMPLETE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.gasmtask.task.domain.TaskCategory;

import org.junit.jupiter.api.Test;

class AchievementCriterionTest {

    private final AchievementMetrics metrics = new AchievementMetrics(12,
            Map.of(TaskCategory.READING, 7, TaskCategory.STUDY, 5), 9, 4, 1);

    @Test
    void cadaCriterioMedeOProprioNumero() {
        assertThat(TOTAL_COMPLETIONS.progressOf(metrics, null)).isEqualTo(12);
        assertThat(CATEGORY_COMPLETIONS.progressOf(metrics, TaskCategory.READING)).isEqualTo(7);
        assertThat(CATEGORY_COMPLETIONS.progressOf(metrics, TaskCategory.SLEEP)).isZero();
        assertThat(SAME_TASK_COMPLETIONS.progressOf(metrics, null)).isEqualTo(9);
        assertThat(STREAK_REACHED.progressOf(metrics, null)).isEqualTo(4);
        assertThat(WEEK_COMPLETE.progressOf(metrics, null)).isEqualTo(1);
    }

    @Test
    void progressoParaNoLimiarEDesbloqueiaAoAlcancar() {
        Achievement ten = Achievement.of("TOTAL_10", TOTAL_COMPLETIONS, 10, null);
        Achievement fifty = Achievement.of("TOTAL_50", TOTAL_COMPLETIONS, 50, null);

        assertThat(ten.progressOf(metrics)).isEqualTo(10);
        assertThat(ten.isReachedBy(metrics)).isTrue();
        assertThat(fifty.progressOf(metrics)).isEqualTo(12);
        assertThat(fifty.isReachedBy(metrics)).isFalse();
    }

    @Test
    void conquistaDeCategoriaContaSoAPropriaCategoria() {
        Achievement reading = Achievement.of("READING_5", CATEGORY_COMPLETIONS, 5, TaskCategory.READING);
        Achievement study = Achievement.of("STUDY_20", CATEGORY_COMPLETIONS, 20, TaskCategory.STUDY);

        assertThat(reading.isReachedBy(metrics)).isTrue();
        assertThat(study.progressOf(metrics)).isEqualTo(5);
        assertThat(study.isReachedBy(metrics)).isFalse();
    }
}
