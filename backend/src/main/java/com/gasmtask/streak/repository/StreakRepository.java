package com.gasmtask.streak.repository;

import java.util.Optional;
import java.util.UUID;

import com.gasmtask.streak.domain.Streak;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StreakRepository extends JpaRepository<Streak, UUID> {

    /** SELECT ... FOR UPDATE: o job e uma requisição nunca fecham os dias do mesmo usuário ao mesmo tempo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Streak s where s.userId = :userId")
    Optional<Streak> findForUpdate(@Param("userId") UUID userId);
}
