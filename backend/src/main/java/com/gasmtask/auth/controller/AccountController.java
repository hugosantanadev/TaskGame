package com.gasmtask.auth.controller;

import com.gasmtask.auth.dto.DeleteAccountRequest;
import com.gasmtask.shared.security.AuthenticatedUser;
import com.gasmtask.user.service.AccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exclusão da conta. Fica com a autenticação porque, além dos dados, encerra a sessão (limpa o cookie). */
@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Perfil")
public class AccountController {

    private final AccountService accountService;
    private final RefreshTokenCookie refreshTokenCookie;

    public AccountController(AccountService accountService, RefreshTokenCookie refreshTokenCookie) {
        this.accountService = accountService;
        this.refreshTokenCookie = refreshTokenCookie;
    }

    @DeleteMapping
    @Operation(summary = "Exclui a conta e todos os dados (pede a senha de novo); não dá para desfazer")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser user,
                                       @RequestBody DeleteAccountRequest request) {
        accountService.delete(user.id(), request.password());
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.clear())
                .build();
    }
}
