package com.gasmtask.auth.dto;

import com.gasmtask.shared.validation.Utf8MaxBytes;
import com.gasmtask.shared.validation.ValidTimeZone;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "informe seu nome")
        @Size(min = 2, max = 40, message = "o nome deve ter entre 2 e 40 caracteres")
        String displayName,

        @NotBlank(message = "informe seu e-mail")
        @Email(message = "e-mail inválido")
        @Size(max = 254, message = "e-mail longo demais")
        String email,

        @NotBlank(message = "crie uma senha")
        @Size(min = 8, message = "a senha precisa de pelo menos 8 caracteres")
        @Utf8MaxBytes(value = 72, message = "a senha é longa demais")
        String password,

        @NotBlank(message = "informe o fuso horário")
        @ValidTimeZone
        String timeZone) {
}
