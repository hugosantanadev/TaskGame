package com.gasmtask.achievement.controller;

import java.util.List;

import com.gasmtask.achievement.dto.AchievementResponse;
import com.gasmtask.achievement.service.AchievementService;
import com.gasmtask.shared.security.AuthenticatedUser;
import com.gasmtask.streak.service.StreakService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/achievements")
@Tag(name = "Conquistas")
public class AchievementController {

    private final AchievementService achievementService;
    private final StreakService streakService;

    public AchievementController(AchievementService achievementService, StreakService streakService) {
        this.achievementService = achievementService;
        this.streakService = streakService;
    }

    @GetMapping
    @Operation(summary = "Todas as conquistas, com progresso e data de desbloqueio")
    public List<AchievementResponse> list(@AuthenticationPrincipal AuthenticatedUser user) {
        // A leitura do streak fecha os dias pendentes antes, então o progresso já sai atualizado
        int longest = streakService.current(user.id()).longest();
        return achievementService.list(user.id(), longest);
    }
}
