package com.gasmtask.gamestate.controller;

import com.gasmtask.gamestate.dto.GameStateResponse;
import com.gasmtask.gamestate.service.GameStateService;
import com.gasmtask.shared.security.AuthenticatedUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/game-state")
@Tag(name = "Jogo")
public class GameStateController {

    private final GameStateService gameStateService;

    public GameStateController(GameStateService gameStateService) {
        this.gameStateService = gameStateService;
    }

    @GetMapping
    @Operation(summary = "Estado consolidado para a camada visual: período do dia, moedas, streak, coleção, quarto e personagem")
    public GameStateResponse gameState(@AuthenticationPrincipal AuthenticatedUser user) {
        return gameStateService.gameState(user.id());
    }
}
