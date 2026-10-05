package com.gasmtask.chest.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.gasmtask.chest.domain.ChestTier;
import com.gasmtask.chest.domain.WeeklyChest;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WeeklyChestRepository extends JpaRepository<WeeklyChest, UUID> {

    boolean existsByUserIdAndWeekStart(UUID userId, LocalDate weekStart);

    Optional<WeeklyChest> findFirstByUserIdAndOpenedAtIsNullAndTierNotOrderByWeekStartAsc(UUID userId, ChestTier tier);

    List<WeeklyChest> findByUserIdAndTierNotOrderByWeekStartDesc(UUID userId, ChestTier tier);

    /** SELECT ... FOR UPDATE: dois toques em "abrir" não pagam o baú duas vezes. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from WeeklyChest c where c.id = :id and c.userId = :userId")
    Optional<WeeklyChest> findForUpdate(@Param("id") UUID id, @Param("userId") UUID userId);
}
