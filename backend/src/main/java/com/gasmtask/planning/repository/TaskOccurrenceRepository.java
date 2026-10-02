package com.gasmtask.planning.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.gasmtask.planning.domain.OccurrenceStatus;
import com.gasmtask.planning.domain.TaskOccurrence;
import com.gasmtask.task.domain.TaskKind;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskOccurrenceRepository extends JpaRepository<TaskOccurrence, UUID> {

    Optional<TaskOccurrence> findByIdAndUserId(UUID id, UUID userId);

    List<TaskOccurrence> findByUserIdAndOccurrenceDate(UUID userId, LocalDate date);

    List<TaskOccurrence> findByUserIdAndOccurrenceDateBetween(UUID userId, LocalDate from, LocalDate to);

    List<TaskOccurrence> findByTaskIdAndStatusAndOccurrenceDateGreaterThanEqual(UUID taskId, OccurrenceStatus status,
                                                                               LocalDate from);

    boolean existsByTaskIdAndOccurrenceDate(UUID taskId, LocalDate date);

    long countByUserIdAndOccurrenceDateAndKind(UUID userId, LocalDate date, TaskKind kind);

    boolean existsByUserIdAndOccurrenceDateAndStatus(UUID userId, LocalDate date, OccurrenceStatus status);

    // ---------------------------------------------------------------- números para as conquistas

    long countByUserIdAndStatus(UUID userId, OccurrenceStatus status);

    @Query("""
            select o.category as category, count(o) as total from TaskOccurrence o
            where o.userId = :userId and o.status = :status
            group by o.category
            """)
    List<CategoryCount> countByCategory(@Param("userId") UUID userId, @Param("status") OccurrenceStatus status);

    @Query("""
            select count(o) from TaskOccurrence o
            where o.userId = :userId and o.status = :status and o.taskId is not null
            group by o.taskId
            """)
    List<Long> countPerTask(@Param("userId") UUID userId, @Param("status") OccurrenceStatus status);

    /** Semanas já encerradas (antes de {@code before}) em que todas as obrigatórias foram concluídas. */
    @Query(value = """
            SELECT COUNT(*) FROM (
                SELECT plan_id FROM task_occurrences
                WHERE user_id = :userId AND kind = 'MANDATORY' AND occurrence_date < :before
                GROUP BY plan_id
                HAVING COUNT(*) = COUNT(*) FILTER (WHERE status = 'COMPLETED')
            ) AS complete_weeks
            """, nativeQuery = true)
    long countCompleteWeeks(@Param("userId") UUID userId, @Param("before") LocalDate before);
}
