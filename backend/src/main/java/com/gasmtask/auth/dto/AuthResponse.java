package com.gasmtask.auth.dto;

import java.time.Instant;

import com.gasmtask.auth.service.AuthSession;
import com.gasmtask.user.dto.UserResponse;

/** O refresh token não aparece aqui: ele vai só no cookie HttpOnly, fora do alcance de JavaScript. */
public record AuthResponse(String accessToken, String tokenType, Instant expiresAt, UserResponse user) {

    public static AuthResponse from(AuthSession session) {
        return new AuthResponse(
                session.accessToken().value(),
                "Bearer",
                session.accessToken().expiresAt(),
                session.user());
    }
}
