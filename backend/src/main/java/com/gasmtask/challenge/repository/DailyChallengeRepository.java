package com.gasmtask.challenge.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.gasmtask.challenge.domain.DailyChallenge;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyChallengeRepository extends JpaRepository<DailyChallenge, UUID> {

    List<DailyChallenge> findByUserIdAndChallengeDate(UUID userId, LocalDate date);
}
