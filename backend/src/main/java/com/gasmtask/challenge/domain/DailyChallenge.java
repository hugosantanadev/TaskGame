package com.gasmtask.challenge.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Um desafio sorteado para um dia. Guarda a meta e a recompensa do momento do sorteio, como a ocorrência guarda
 * o retrato da missão (RN10): mudar a configuração depois não muda o desafio de hoje.
 */
@Entity
@Table(name = "daily_challenges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyChallenge {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "challenge_date", nullable = false, updatable = false)
    private LocalDate challengeDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private ChallengeType code;

    @Column(nullable = false, updatable = false)
    private int target;

    @Column(name = "xp_reward", nullable = false, updatable = false)
    private int xpReward;

    @Column(name = "coin_reward", nullable = false, updatable = false)
    private int coinReward;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static DailyChallenge draw(UUID userId, LocalDate date, ChallengeType type, int xpReward, int coinReward,
                                      Instant now) {
        DailyChallenge challenge = new DailyChallenge();
        challenge.id = UUID.randomUUID();
        challenge.userId = userId;
        challenge.challengeDate = date;
        challenge.code = type;
        challenge.target = type.target();
        challenge.xpReward = xpReward;
        challenge.coinReward = coinReward;
        challenge.createdAt = now;
        return challenge;
    }

    public boolean isCompleted() {
        return completedAt != null;
    }

    public void complete(Instant now) {
        if (!isCompleted()) {
            completedAt = now;
        }
    }
}
