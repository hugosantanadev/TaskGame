package com.gasmtask.planning.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;

import com.gasmtask.economy.domain.Reward;
import com.gasmtask.task.domain.Task;
import com.gasmtask.task.domain.TaskCategory;
import com.gasmtask.task.domain.TaskKind;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Uma tarefa num dia: é o que se conclui, se perde e se recompensa. Guarda um retrato do que valia
 * quando foi planejada (RN10), então editar a missão depois não muda o histórico nem o que já está em hoje.
 * Extras avulsas não têm missão ({@code taskId} nulo).
 */
@Entity
@Table(name = "task_occurrences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskOccurrence {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(name = "task_id", updatable = false)
    private UUID taskId;

    @Column(name = "occurrence_date", nullable = false)
    private LocalDate occurrenceDate;

    @Column(name = "planned_time")
    private LocalTime plannedTime;

    @Column(nullable = false, length = 60)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private TaskCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    private TaskKind kind;

    @Column(nullable = false, updatable = false)
    private int points;

    @Column(name = "base_coins", nullable = false, updatable = false)
    private int baseCoins;

    @Column(name = "duration_minutes", updatable = false)
    private Integer durationMinutes;

    @Column(name = "requires_proof", nullable = false, updatable = false)
    private boolean requiresProof;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OccurrenceStatus status;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "on_time")
    private Boolean onTime;

    @Column(name = "earned_points")
    private Integer earnedPoints;

    @Column(name = "earned_coins")
    private Integer earnedCoins;

    @Column(name = "proof_attached", nullable = false)
    private boolean proofAttached;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private Long version;

    public static TaskOccurrence fromTask(Task task, WeeklyPlan plan, LocalDate date, LocalTime time,
                                          int baseCoins, Instant now) {
        TaskOccurrence occurrence = base(task.getUserId(), plan, date, time, now);
        occurrence.taskId = task.getId();
        occurrence.title = task.getName();
        occurrence.category = task.getCategory();
        occurrence.kind = task.getKind();
        occurrence.points = task.getPoints();
        occurrence.baseCoins = baseCoins;
        occurrence.durationMinutes = task.getDurationMinutes();
        occurrence.requiresProof = task.isRequiresProof();
        return occurrence;
    }

    public static TaskOccurrence extra(UUID userId, WeeklyPlan plan, String title, TaskCategory category, int points,
                                       int baseCoins, LocalDate date, LocalTime time, Integer durationMinutes,
                                       boolean requiresProof, Instant now) {
        TaskOccurrence occurrence = base(userId, plan, date, time, now);
        occurrence.title = title;
        occurrence.category = category;
        occurrence.kind = TaskKind.EXTRA;
        occurrence.points = points;
        occurrence.baseCoins = baseCoins;
        occurrence.durationMinutes = durationMinutes;
        occurrence.requiresProof = requiresProof;
        return occurrence;
    }

    private static TaskOccurrence base(UUID userId, WeeklyPlan plan, LocalDate date, LocalTime time, Instant now) {
        if (!plan.contains(date)) {
            throw new IllegalArgumentException("A data " + date + " não pertence à semana " + plan.getWeekStart());
        }
        TaskOccurrence occurrence = new TaskOccurrence();
        occurrence.id = UUID.randomUUID();
        occurrence.userId = userId;
        occurrence.planId = plan.getId();
        occurrence.occurrenceDate = date;
        occurrence.plannedTime = time;
        occurrence.status = OccurrenceStatus.PENDING;
        occurrence.createdAt = now;
        return occurrence;
    }

    public boolean isPending() {
        return status == OccurrenceStatus.PENDING;
    }

    public boolean isCompleted() {
        return status == OccurrenceStatus.COMPLETED;
    }

    public boolean isMandatory() {
        return kind == TaskKind.MANDATORY;
    }

    /** Foi planejada antes do próprio dia? Condição do bônus de pontualidade. */
    public boolean plannedInAdvance(ZoneId zone) {
        return LocalDate.ofInstant(createdAt, zone).isBefore(occurrenceDate);
    }

    public void complete(Instant at, boolean onTime, Reward reward) {
        if (!isPending()) {
            throw new IllegalStateException("Só uma tarefa pendente pode ser concluída");
        }
        this.status = OccurrenceStatus.COMPLETED;
        this.completedAt = at;
        this.onTime = onTime;
        this.earnedPoints = reward.points();
        this.earnedCoins = reward.totalCoins();
    }

    public void markProofAttached() {
        this.proofAttached = true;
    }

    /** Bônus pago depois da conclusão (prova enviada mais tarde, RN20). */
    public void addEarnedCoins(int coins) {
        this.earnedCoins = (earnedCoins == null ? 0 : earnedCoins) + coins;
    }

    public void markMissed() {
        if (isPending()) {
            this.status = OccurrenceStatus.MISSED;
        }
    }

    public void moveTo(UUID planId, LocalDate date, LocalTime time) {
        this.planId = planId;
        this.occurrenceDate = date;
        this.plannedTime = time;
    }

    public void rename(String title) {
        this.title = title;
    }
}
