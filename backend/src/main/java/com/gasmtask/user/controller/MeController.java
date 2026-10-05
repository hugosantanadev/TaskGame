package com.gasmtask.user.controller;

import com.gasmtask.shared.security.AuthenticatedUser;
import com.gasmtask.user.dto.ReminderSettingsRequest;
import com.gasmtask.user.dto.ReminderSettingsResponse;
import com.gasmtask.user.dto.UpdateProfileRequest;
import com.gasmtask.user.dto.UserResponse;
import com.gasmtask.user.service.AccountService;
import com.gasmtask.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Perfil")
public class MeController {

    private final UserService userService;
    private final AccountService accountService;

    public MeController(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @GetMapping("/export")
    @Operation(summary = "Baixa todos os seus dados num JSON (LGPD: portabilidade)")
    public ResponseEntity<String> export(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"gasmtask-meus-dados.json\"")
                .body(accountService.export(user.id()));
    }

    @GetMapping
    @Operation(summary = "Perfil do usuário autenticado")
    public UserResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        return userService.getProfile(user.id());
    }

    @PatchMapping
    @Operation(summary = "Atualiza nome, fuso horário ou visibilidade no ranking")
    public UserResponse update(@AuthenticationPrincipal AuthenticatedUser user,
                               @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(user.id(), request);
    }

    @GetMapping("/reminder-settings")
    @Operation(summary = "Preferências de lembrete: tarefas, antecedência, hora de dormir e de acordar")
    public ReminderSettingsResponse reminderSettings(@AuthenticationPrincipal AuthenticatedUser user) {
        return userService.reminderSettings(user.id());
    }

    @PutMapping("/reminder-settings")
    @Operation(summary = "Substitui as preferências de lembrete (horário nulo desliga aquele lembrete)")
    public ReminderSettingsResponse updateReminderSettings(@AuthenticationPrincipal AuthenticatedUser user,
                                                           @Valid @RequestBody ReminderSettingsRequest request) {
        return userService.updateReminderSettings(user.id(), request);
    }
}
