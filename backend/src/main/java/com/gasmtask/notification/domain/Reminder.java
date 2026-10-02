package com.gasmtask.notification.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Um lembrete calculado.
 *
 * @param notifyAt     quando avisar
 * @param eventAt      quando a coisa acontece (início da tarefa, hora de dormir ou de acordar)
 * @param occurrenceId a tarefa lembrada; nulo para dormir e acordar
 * @param title        nome da tarefa; nulo para dormir e acordar (o texto do aviso é do app)
 */
public record Reminder(ReminderKind kind, Instant notifyAt, Instant eventAt, UUID occurrenceId, String title) {
}
