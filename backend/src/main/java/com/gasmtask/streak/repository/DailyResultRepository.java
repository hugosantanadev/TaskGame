package com.gasmtask.streak.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.gasmtask.streak.domain.DailyResult;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyResultRepository extends JpaRepository<DailyResult, UUID> {

    List<DailyResult> findByUserIdAndResultDateBetweenOrderByResultDateAsc(UUID userId, LocalDate from, LocalDate to);
}
