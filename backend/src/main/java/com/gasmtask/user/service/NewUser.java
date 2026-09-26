package com.gasmtask.user.service;

/** Dados para criar um usuário. A senha já chega como hash: quem cuida de senha é o módulo de autenticação. */
public record NewUser(String email, String passwordHash, String displayName, String timeZone) {
}
