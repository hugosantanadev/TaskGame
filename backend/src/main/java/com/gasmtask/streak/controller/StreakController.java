package com.gasmtask.streak.controller;

import com.gasmtask.shared.security.AuthenticatedUser;
import com.gasmtask.streak.dto.StreakResponse;
import com.gasmtask.streak.service.StreakService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/streak")
@Tag(name = "Streak")
public class StreakController {

    private final StreakService streakService;

    public StreakController(StreakService streakService) {
        this.streakService = streakService;
    }

    @GetMapping
    @Operation(summary = "Streak atual, maior streak e situação de hoje")
    public StreakResponse current(@AuthenticationPrincipal AuthenticatedUser user) {
        return streakService.current(user.id());
    }

    @PostMapping("/freezes")
    @Operation(summary = "Compra um protetor de sequência com moedas (salva um dia de falha na virada do dia)")
    public StreakResponse buyFreeze(@AuthenticationPrincipal AuthenticatedUser user) {
        return streakService.buyFreeze(user.id());
    }
}
