package com.gasmtask.progression.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.gasmtask.progression.domain.XpEvent;
import com.gasmtask.progression.domain.XpReason;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XpEventRepository extends JpaRepository<XpEvent, UUID> {

    boolean existsByOccurrenceIdAndReason(UUID occurrenceId, XpReason reason);

    boolean existsByUserIdAndEventDateAndReason(UUID userId, LocalDate eventDate, XpReason reason);

    List<XpEvent> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
