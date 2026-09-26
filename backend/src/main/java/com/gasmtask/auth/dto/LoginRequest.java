package com.gasmtask.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "informe seu e-mail") String email,
        @NotBlank(message = "informe sua senha") String password) {
}
