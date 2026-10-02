package com.gasmtask.notification.domain;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Calcula os lembretes que saem numa janela de tempo (RF23), sem Spring e sem banco:
 * <ul>
 *   <li>tarefa: {@code leadMinutes} antes do horário planejado (só tarefas pendentes e com horário);</li>
 *   <li>dormir: {@code leadMinutes} antes da hora de dormir;</li>
 *   <li>acordar: na hora de acordar.</li>
 * </ul>
 * Os horários são do fuso do usuário; a conversão para instante respeita o horário de verão.
 */
public final class ReminderPlanner {

    private static final Comparator<Reminder> BY_TIME =
            Comparator.comparing(Reminder::notifyAt).thenComparing(Reminder::kind);

    private ReminderPlanner() {
    }

    /** Lembretes com aviso em [now, now + window), do mais próximo ao mais distante. */
    public static List<Reminder> plan(ZonedDateTime now, Duration window, ReminderPreferences preferences,
                                      Collection<PlannedTask> tasks) {
        Instant from = now.toInstant();
        Instant until = from.plus(window);
        ZoneId zone = now.getZone();
        Duration lead = Duration.ofMinutes(preferences.leadMinutes());
        List<Reminder> reminders = new ArrayList<>();

        if (preferences.tasksEnabled()) {
            for (PlannedTask task : tasks) {
                Instant start = at(task.date(), task.time(), zone);
                reminders.add(new Reminder(ReminderKind.TASK, start.minus(lead), start, task.occurrenceId(),
                        task.title()));
            }
        }
        // Um dia de folga para cada lado: com a antecedência, o aviso de dormir pode cair no dia anterior
        LocalDate last = LocalDate.ofInstant(until, zone).plusDays(1);
        for (LocalDate date = now.toLocalDate().minusDays(1); !date.isAfter(last); date = date.plusDays(1)) {
            if (preferences.bedtime() != null) {
                Instant bedtime = at(date, preferences.bedtime(), zone);
                reminders.add(new Reminder(ReminderKind.BEDTIME, bedtime.minus(lead), bedtime, null, null));
            }
            if (preferences.wakeTime() != null) {
                Instant wake = at(date, preferences.wakeTime(), zone);
                reminders.add(new Reminder(ReminderKind.WAKE_UP, wake, wake, null, null));
            }
        }

        return reminders.stream()
                .filter(reminder -> !reminder.notifyAt().isBefore(from) && reminder.notifyAt().isBefore(until))
                .sorted(BY_TIME)
                .toList();
    }

    private static Instant at(LocalDate date, LocalTime time, ZoneId zone) {
        return ZonedDateTime.of(date, time, zone).toInstant();
    }

    /** O que o planejador precisa saber das preferências do usuário. */
    public record ReminderPreferences(boolean tasksEnabled, int leadMinutes, LocalTime bedtime, LocalTime wakeTime) {
    }

    /** Uma tarefa pendente com horário, no fuso do usuário. */
    public record PlannedTask(UUID occurrenceId, String title, LocalDate date, LocalTime time) {
    }
}
