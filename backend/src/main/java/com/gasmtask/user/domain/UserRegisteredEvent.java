package com.gasmtask.user.domain;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Publicado no cadastro, dentro da mesma transação. Cada módulo que precisa de dados iniciais
 * (carteira, streak, quarto, personagem) escuta este evento, e o cadastro não precisa conhecê-los.
 */
public record UserRegisteredEvent(UUID userId, ZoneId zone, Instant registeredAt) {
}
