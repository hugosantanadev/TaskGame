package com.gasmtask.auth.controller;

import java.net.URI;

import com.gasmtask.auth.dto.AuthResponse;
import com.gasmtask.auth.dto.LoginRequest;
import com.gasmtask.auth.dto.RegisterRequest;
import com.gasmtask.auth.service.AuthService;
import com.gasmtask.auth.service.AuthSession;
import com.gasmtask.shared.exception.BusinessException;
import com.gasmtask.shared.exception.ErrorCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookie refreshTokenCookie;

    public AuthController(AuthService authService, RefreshTokenCookie refreshTokenCookie) {
        this.authService = authService;
        this.refreshTokenCookie = refreshTokenCookie;
    }

    @PostMapping("/register")
    @Operation(summary = "Cria a conta e já abre a sessão")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthSession session = authService.register(request);
        return ResponseEntity.created(URI.create("/api/v1/me"))
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.create(session.refreshToken().value()))
                .body(AuthResponse.from(session));
    }

    @PostMapping("/login")
    @Operation(summary = "Entra com e-mail e senha")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return withSession(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Troca o refresh token do cookie por um novo par de tokens")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = refreshTokenCookie.read(request)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        try {
            return withSession(authService.refresh(rawToken));
        } catch (BusinessException ex) {
            // Cookie inválido não serve para nada: apaga junto com a resposta 401.
            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.clear());
            throw ex;
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Encerra a sessão deste dispositivo")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        refreshTokenCookie.read(request).ifPresent(authService::logout);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.clear())
                .build();
    }

    private ResponseEntity<AuthResponse> withSession(AuthSession session) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.create(session.refreshToken().value()))
                .body(AuthResponse.from(session));
    }
}
