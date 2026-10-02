package com.gasmtask.completion.repository;

import java.util.Optional;
import java.util.UUID;

import com.gasmtask.completion.domain.Proof;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProofRepository extends JpaRepository<Proof, UUID> {

    Optional<Proof> findByOccurrenceIdAndUserId(UUID occurrenceId, UUID userId);
}
