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

    // ---------------------------------------------------------------- números para as estatísticas

    /** Pontos das concluídas por categoria: o treino de cada atributo do personagem. */
    @Query("""
            select o.category as category, coalesce(sum(o.earnedPoints), 0) as points from TaskOccurrence o
            where o.userId = :userId and o.status = com.gasmtask.planning.domain.OccurrenceStatus.COMPLETED
            group by o.category
            """)
    List<CategoryPoints> sumPointsByCategory(@Param("userId") UUID userId);

    @Query("select coalesce(sum(o.earnedPoints), 0) from TaskOccurrence o where o.userId = :userId")
    long sumEarnedPoints(@Param("userId") UUID userId);

    /** Uma linha por (dia, tipo, situação) no intervalo: base do histórico e do resumo semanal. */
    @Query("""
            select o.occurrenceDate as date, o.kind as kind, o.status as status, count(o) as total,
                coalesce(sum(o.earnedPoints), 0) as points, coalesce(sum(o.earnedCoins), 0) as coins
            from TaskOccurrence o
            where o.userId = :userId and o.occurrenceDate between :from and :to
            group by o.occurrenceDate, o.kind, o.status
            """)
    List<DailyCount> countByDay(@Param("userId") UUID userId, @Param("from") LocalDate from,
                                @Param("to") LocalDate to);

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
