package com.gasmtask.planning.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import com.gasmtask.planning.domain.WeeklyPlan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WeeklyPlanRepository extends JpaRepository<WeeklyPlan, UUID> {

    Optional<WeeklyPlan> findByUserIdAndWeekStart(UUID userId, LocalDate weekStart);

    /** Retorna 1 se criou a semana e 0 se ela já existia (inclusive criada agora por outra requisição). */
    @Modifying
    @Query(value = """
            INSERT INTO weekly_plans (id, user_id, week_start, generated_at)
            VALUES (:id, :userId, :weekStart, :now)
            ON CONFLICT (user_id, week_start) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id, @Param("userId") UUID userId,
                       @Param("weekStart") LocalDate weekStart, @Param("now") Instant now);

    @Modifying
    @Query("""
            update WeeklyPlan p set p.closedAt = :now
            where p.userId = :userId and p.closedAt is null and p.weekStart <= :lastWeekStart
            """)
    int closeWeeksUpTo(@Param("userId") UUID userId, @Param("lastWeekStart") LocalDate lastWeekStart,
                       @Param("now") Instant now);
}
