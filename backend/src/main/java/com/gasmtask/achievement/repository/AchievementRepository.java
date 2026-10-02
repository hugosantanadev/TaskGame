package com.gasmtask.achievement.repository;

import java.util.List;
import java.util.UUID;

import com.gasmtask.achievement.domain.Achievement;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AchievementRepository extends JpaRepository<Achievement, UUID> {

    List<Achievement> findAllByOrderBySortOrderAsc();
}
