package com.gasmtask.user.controller;

import com.gasmtask.shared.security.AuthenticatedUser;
import com.gasmtask.user.dto.UpdateProfileRequest;
import com.gasmtask.user.dto.UserResponse;
import com.gasmtask.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Perfil")
public class MeController {

    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
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
}
