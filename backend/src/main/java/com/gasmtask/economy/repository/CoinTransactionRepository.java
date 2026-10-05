package com.gasmtask.economy.repository;

import java.util.UUID;

import com.gasmtask.economy.domain.CoinTransaction;
import com.gasmtask.economy.domain.CoinTransactionReason;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoinTransactionRepository extends JpaRepository<CoinTransaction, UUID> {

    boolean existsByOccurrenceIdAndReason(UUID occurrenceId, CoinTransactionReason reason);

    boolean existsByChallengeId(UUID challengeId);

    boolean existsByChestId(UUID chestId);

    Page<CoinTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
