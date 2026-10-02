package com.gasmtask.streak.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.gasmtask.planning.domain.DayProgress;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Fechamento de um dia: histórico do streak e base das estatísticas diárias. */
@Entity
@Table(name = "daily_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyResult {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "result_date", nullable = false, updatable = false)
    private LocalDate resultDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    private DayStatus status;

    @Column(name = "mandatory_planned", nullable = false, updatable = false)
    private int mandatoryPlanned;

    @Column(name = "mandatory_done", nullable = false, updatable = false)
    private int mandatoryDone;

    @Column(name = "extras_done", nullable = false, updatable = false)
    private int extrasDone;

    @Column(nullable = false, updatable = false)
    private int points;

    @Column(nullable = false, updatable = false)
    private int coins;

    @Column(name = "streak_after", nullable = false, updatable = false)
    private int streakAfter;

    @Column(name = "closed_at", nullable = false, updatable = false)
    private Instant closedAt;

    public static DailyResult of(UUID userId, LocalDate date, DayStatus status, DayProgress progress, int streakAfter,
                                 Instant now) {
        DailyResult result = new DailyResult();
        result.id = UUID.randomUUID();
        result.userId = userId;
        result.resultDate = date;
        result.status = status;
        result.mandatoryPlanned = progress.mandatoryPlanned();
        result.mandatoryDone = progress.mandatoryDone();
        result.extrasDone = progress.extrasDone();
        result.points = progress.points();
        result.coins = progress.coins();
        result.streakAfter = streakAfter;
        result.closedAt = now;
        return result;
    }
}
