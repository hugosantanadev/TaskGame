package com.gasmtask.today.controller;

import com.gasmtask.shared.security.AuthenticatedUser;
import com.gasmtask.today.dto.TodayResponse;
import com.gasmtask.today.service.TodayService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/today")
@Tag(name = "Hoje")
public class TodayController {

    private final TodayService todayService;

    public TodayController(TodayService todayService) {
        this.todayService = todayService;
    }

    @GetMapping
    @Operation(summary = "Tela Hoje: tarefas por horário, próxima tarefa, progresso, saldo e streak")
    public TodayResponse today(@AuthenticationPrincipal AuthenticatedUser user) {
        return todayService.today(user.id());
    }
}
