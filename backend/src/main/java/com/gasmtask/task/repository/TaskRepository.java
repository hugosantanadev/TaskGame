package com.gasmtask.task.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.gasmtask.task.domain.Task;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    @EntityGraph(attributePaths = "schedule")
    Optional<Task> findByIdAndUserId(UUID id, UUID userId);

    @EntityGraph(attributePaths = "schedule")
    List<Task> findByUserIdAndArchivedAtIsNullOrderByCreatedAtAsc(UUID userId);

    @EntityGraph(attributePaths = "schedule")
    List<Task> findByUserIdAndArchivedAtIsNotNullOrderByArchivedAtDesc(UUID userId);

    long countByUserIdAndArchivedAtIsNull(UUID userId);
}
