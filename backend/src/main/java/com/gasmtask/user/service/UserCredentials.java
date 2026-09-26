package com.gasmtask.user.service;

import java.util.UUID;

/** Visão mínima usada no login, para o módulo de autenticação não depender da entidade {@code User}. */
public record UserCredentials(UUID userId, String passwordHash) {
}
