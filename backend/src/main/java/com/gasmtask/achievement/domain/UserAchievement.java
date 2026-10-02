package com.gasmtask.achievement.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Desbloqueio: único por usuário e conquista, e permanente (RN27). */
@Entity
@Table(name = "user_achievements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAchievement {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "achievement_id", nullable = false, updatable = false)
    private UUID achievementId;

    @Column(name = "unlocked_at", nullable = false, updatable = false)
    private Instant unlockedAt;

    public static UserAchievement unlock(UUID userId, UUID achievementId, Instant now) {
        UserAchievement unlocked = new UserAchievement();
        unlocked.id = UUID.randomUUID();
        unlocked.userId = userId;
        unlocked.achievementId = achievementId;
        unlocked.unlockedAt = now;
        return unlocked;
    }
}
