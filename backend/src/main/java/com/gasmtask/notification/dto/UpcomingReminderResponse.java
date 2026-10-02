package com.gasmtask.notification.dto;

import java.time.Instant;
import java.util.UUID;

import com.gasmtask.notification.domain.Reminder;
import com.gasmtask.notification.domain.ReminderKind;

/**
 * Um lembrete a agendar. O texto do aviso é montado pelo app a partir do tipo.
 *
 * @param notifyAt     quando avisar
 * @param eventAt      quando a tarefa começa, ou a hora de dormir ou de acordar
 * @param occurrenceId a tarefa lembrada; nulo para dormir e acordar
 * @param title        nome da tarefa; nulo para dormir e acordar
 */
public record UpcomingReminderResponse(ReminderKind kind, Instant notifyAt, Instant eventAt, UUID occurrenceId,
                                       String title) {

    public static UpcomingReminderResponse of(Reminder reminder) {
        return new UpcomingReminderResponse(reminder.kind(), reminder.notifyAt(), reminder.eventAt(),
                reminder.occurrenceId(), reminder.title());
    }
}
