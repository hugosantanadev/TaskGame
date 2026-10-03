package com.gasmtask.progression.repository;

import java.util.Optional;
import java.util.UUID;

import com.gasmtask.progression.domain.PlayerProgress;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerProgressRepository extends JpaRepository<PlayerProgress, UUID> {

    /** SELECT ... FOR UPDATE: duas mudanças de XP do mesmo usuário nunca se atropelam. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PlayerProgress p where p.userId = :userId")
    Optional<PlayerProgress> findForUpdate(@Param("userId") UUID userId);
}
