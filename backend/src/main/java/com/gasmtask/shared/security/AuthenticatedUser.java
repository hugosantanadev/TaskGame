package com.gasmtask.shared.security;

import java.util.UUID;

import org.springframework.security.core.AuthenticatedPrincipal;

/**
 * Usuário autenticado extraído do JWT. Controllers recebem via {@code @AuthenticationPrincipal}
 * e usam {@link #id()} para filtrar todos os dados pelo dono.
 */
public record AuthenticatedUser(UUID id) implements AuthenticatedPrincipal {

    @Override
    public String getName() {
        return id.toString();
    }
}
