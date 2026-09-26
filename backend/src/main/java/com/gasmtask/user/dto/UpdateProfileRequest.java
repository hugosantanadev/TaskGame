package com.gasmtask.user.dto;

import com.gasmtask.shared.validation.ValidTimeZone;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Atualização parcial do perfil: campos nulos ficam como estão. */
public record UpdateProfileRequest(
        @Size(min = 2, max = 40, message = "o nome deve ter entre 2 e 40 caracteres")
        @Pattern(regexp = ".*\\S.*", message = "o nome não pode ficar em branco")
        String displayName,

        @ValidTimeZone
        String timeZone,

        Boolean rankingVisible) {
}
