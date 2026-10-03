package com.gasmtask.progression.controller;

import com.gasmtask.progression.dto.ProgressionResponse;
import com.gasmtask.progression.service.ProgressionService;
import com.gasmtask.shared.security.AuthenticatedUser;
import com.gasmtask.streak.service.DayClosingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/rank")
@Tag(name = "Elo")
public class ProgressionController {

    private final ProgressionService progressionService;
    private final DayClosingService closing;

    public ProgressionController(ProgressionService progressionService, DayClosingService closing) {
        this.progressionService = progressionService;
        this.closing = closing;
    }

    @GetMapping
    @Operation(summary = "Elo e XP: elo atual, maior elo alcançado, escada, roupas de cada elo e últimas mudanças de XP")
    public ProgressionResponse rank(@AuthenticationPrincipal AuthenticatedUser user) {
        // As obrigatórias perdidas custam XP na virada do dia: fecha os dias pendentes antes de mostrar
        closing.closePendingDays(user.id());
        return progressionService.view(user.id());
    }
}
