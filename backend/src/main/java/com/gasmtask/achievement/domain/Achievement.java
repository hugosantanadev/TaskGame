package com.gasmtask.achievement.domain;

import java.util.UUID;

import com.gasmtask.task.domain.TaskCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Conquista do catálogo (seed de migration): critério, limiar e, quando o critério pede, a categoria. */
@Entity
@Table(name = "achievements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Achievement {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 40, updatable = false)
    private String code;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(nullable = false, length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AchievementCriterion criterion;

    @Column(nullable = false)
    private int threshold;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TaskCategory category;

    @Column(name = "asset_key", length = 60)
    private String assetKey;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    /** O catálogo nasce na migration; esta fábrica serve a testes. */
    public static Achievement of(String code, AchievementCriterion criterion, int threshold, TaskCategory category) {
        Achievement achievement = new Achievement();
        achievement.id = UUID.randomUUID();
        achievement.code = code;
        achievement.name = code;
        achievement.description = code;
        achievement.criterion = criterion;
        achievement.threshold = threshold;
        achievement.category = category;
        return achievement;
    }

    /** Progresso para a tela, limitado ao limiar (15 de 10 vira 10 de 10). */
    public int progressOf(AchievementMetrics metrics) {
        return Math.min(threshold, criterion.progressOf(metrics, category));
    }

    public boolean isReachedBy(AchievementMetrics metrics) {
        return criterion.progressOf(metrics, category) >= threshold;
    }
}
