package com.gasmtask.chest.controller;

import java.util.List;
import java.util.UUID;

import com.gasmtask.chest.dto.ChestResponse;
import com.gasmtask.chest.dto.OpenedChestResponse;
import com.gasmtask.chest.service.ChestService;
import com.gasmtask.shared.security.AuthenticatedUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chests")
@Tag(name = "Baú semanal")
public class ChestController {

    private final ChestService chestService;

    public ChestController(ChestService chestService) {
        this.chestService = chestService;
    }

    @GetMapping
    @Operation(summary = "Baús semanais, do mais recente ao mais antigo (abertos e fechados)")
    public List<ChestResponse> history(@AuthenticationPrincipal AuthenticatedUser user) {
        return chestService.history(user.id());
    }

    @PostMapping("/{id}/open")
    @Operation(summary = "Abre um baú: moedas, XP e, no ouro e no lendário, um item que você ainda não tem")
    public OpenedChestResponse open(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
        return chestService.open(user.id(), id);
    }
}
