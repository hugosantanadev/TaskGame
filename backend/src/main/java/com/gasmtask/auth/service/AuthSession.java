package com.gasmtask.auth.service;

import com.gasmtask.auth.service.RefreshTokenService.IssuedRefreshToken;
import com.gasmtask.shared.security.AccessTokenService.IssuedAccessToken;
import com.gasmtask.user.dto.UserResponse;

/** Resultado de cadastro, login ou renovação: o controller devolve o access token no corpo e o refresh no cookie. */
public record AuthSession(IssuedAccessToken accessToken, IssuedRefreshToken refreshToken, UserResponse user) {
}
