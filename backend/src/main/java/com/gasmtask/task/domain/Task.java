package com.gasmtask.task.domain;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Missão: o que o usuário quer fazer e em quais dias. As ocorrências datadas são geradas a partir dela
 * pelo módulo de planejamento. Missões não são apagadas, só arquivadas, para o histórico continuar íntegro.
 */
@Entity
@Table(name = "tasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(length = 280)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TaskKind kind;

    @Column(nullable = false)
    private int points;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "requires_proof", nullable = false)
    private boolean requiresProof;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskSchedule> schedule = new ArrayList<>();

    @Column(name = "archived_at")
    private Instant archivedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public static Task create(UUID userId, TaskDefinition definition, Instant now) {
        Task task = new Task();
        task.id = UUID.randomUUID();
        task.userId = userId;
        task.createdAt = now;
        task.apply(definition, now);
        return task;
    }

    /** Aplica a edição e informa o que mudou, para o planejamento refazer só o necessário (RN09). */
    public TaskChange update(TaskDefinition definition, Instant now) {
        boolean titleChanged = !name.equals(definition.name());
        boolean planChanged = category != definition.category()
                || kind != definition.kind()
                || points != definition.points()
                || !Objects.equals(durationMinutes, definition.durationMinutes())
                || requiresProof != definition.requiresProof()
                || !slots().equals(sorted(definition.slots()));
        apply(definition, now);
        return new TaskChange(planChanged, titleChanged);
    }

    public void archive(Instant now) {
        if (archivedAt == null) {
            archivedAt = now;
            updatedAt = now;
        }
    }

    public boolean isArchived() {
        return archivedAt != null;
    }

    public Optional<ScheduleSlot> slotOn(DayOfWeek day) {
        return schedule.stream().filter(entry -> entry.getDayOfWeek() == day).map(TaskSchedule::toSlot).findFirst();
    }

    /** Recorrência em ordem de segunda a domingo. */
    public List<ScheduleSlot> slots() {
        return sorted(schedule.stream().map(TaskSchedule::toSlot).toList());
    }

    private void apply(TaskDefinition definition, Instant now) {
        name = definition.name();
        description = definition.description();
        category = definition.category();
        kind = definition.kind();
        points = definition.points();
        durationMinutes = definition.durationMinutes();
        requiresProof = definition.requiresProof();
        mergeSchedule(definition.slots());
        updatedAt = now;
    }

    /** Atualiza a recorrência no lugar: mantém os dias que continuam, remove os que saíram e cria os novos. */
    private void mergeSchedule(List<ScheduleSlot> slots) {
        Map<DayOfWeek, ScheduleSlot> wanted = slots.stream()
                .collect(Collectors.toMap(ScheduleSlot::day, Function.identity(), (first, second) -> {
                    throw new IllegalArgumentException("Dia repetido na recorrência: " + first.day());
                }));
        schedule.removeIf(entry -> !wanted.containsKey(entry.getDayOfWeek()));
        for (ScheduleSlot slot : wanted.values()) {
            schedule.stream()
                    .filter(entry -> entry.getDayOfWeek() == slot.day())
                    .findFirst()
                    .ifPresentOrElse(entry -> entry.changeTime(slot.time()), () -> schedule.add(TaskSchedule.of(this, slot)));
        }
    }

    private static List<ScheduleSlot> sorted(List<ScheduleSlot> slots) {
        return slots.stream().sorted(Comparator.comparing(ScheduleSlot::day)).toList();
    }
}
