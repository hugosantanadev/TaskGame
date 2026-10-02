package com.gasmtask.achievement.repository;

import java.util.List;
import java.util.UUID;

import com.gasmtask.achievement.domain.UserAchievement;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {

    List<UserAchievement> findByUserId(UUID userId);
}
