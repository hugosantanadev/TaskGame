package com.gasmtask.planning.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Uma semana (segunda a domingo) do usuário. É criada por INSERT ... ON CONFLICT DO NOTHING no repositório,
 * para que duas requisições simultâneas nunca criem a mesma semana duas vezes.
 */
@Entity
@Table(name = "weekly_plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyPlan {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "week_start", nullable = false, updatable = false)
    private LocalDate weekStart;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private Instant generatedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    /** Em produção as semanas nascem pelo INSERT idempotente do repositório; isto serve a testes e importações. */
    public static WeeklyPlan create(UUID userId, LocalDate weekStart, Instant now) {
        if (weekStart.getDayOfWeek() != java.time.DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("A semana começa na segunda-feira: " + weekStart);
        }
        WeeklyPlan plan = new WeeklyPlan();
        plan.id = UUID.randomUUID();
        plan.userId = userId;
        plan.weekStart = weekStart;
        plan.generatedAt = now;
        return plan;
    }

    public LocalDate weekEnd() {
        return weekStart.plusDays(6);
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(weekStart) && !date.isAfter(weekEnd());
    }
}
