package com.gasmtask.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.gasmtask.notification.domain.ReminderPlanner.PlannedTask;
import com.gasmtask.notification.domain.ReminderPlanner.ReminderPreferences;

import org.junit.jupiter.api.Test;

class ReminderPlannerTest {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 30);
    private static final ZonedDateTime EVENING = TODAY.atTime(20, 0).atZone(SAO_PAULO);
    private static final Duration DAY = Duration.ofHours(24);

    private static PlannedTask task(String title, LocalDate date, int hour, int minute) {
        return new PlannedTask(UUID.randomUUID(), title, date, LocalTime.of(hour, minute));
    }

    private static Instant at(LocalDate date, int hour, int minute) {
        return LocalDateTime.of(date, LocalTime.of(hour, minute)).atZone(SAO_PAULO).toInstant();
    }

    @Test
    void avisaComAntecedenciaSoDentroDaJanelaEEmOrdem() {
        ReminderPreferences preferences = new ReminderPreferences(true, 10, LocalTime.of(23, 30), LocalTime.of(6, 30));
        List<PlannedTask> tasks = List.of(
                task("Bíblia", TODAY, 21, 0),                 // avisa 20:50
                task("Já começando", TODAY, 20, 5),           // aviso às 19:55 já passou
                task("Estudar Java", TODAY.plusDays(1), 8, 0),  // avisa 07:50 de amanhã
                task("Longe demais", TODAY.plusDays(2), 8, 0));

        List<Reminder> reminders = ReminderPlanner.plan(EVENING, DAY, preferences, tasks);

        assertThat(reminders).extracting(Reminder::kind).containsExactly(
                ReminderKind.TASK, ReminderKind.BEDTIME, ReminderKind.WAKE_UP, ReminderKind.TASK);
        assertThat(reminders).extracting(Reminder::notifyAt).containsExactly(
                at(TODAY, 20, 50), at(TODAY, 23, 20), at(TODAY.plusDays(1), 6, 30), at(TODAY.plusDays(1), 7, 50));
        assertThat(reminders.getFirst().eventAt()).isEqualTo(at(TODAY, 21, 0));
        assertThat(reminders.getFirst().title()).isEqualTo("Bíblia");
        assertThat(reminders.get(1).eventAt()).isEqualTo(at(TODAY, 23, 30));
        assertThat(reminders.get(1).occurrenceId()).isNull();
    }

    @Test
    void semTarefasLigadasSobramDormirEAcordar() {
        ReminderPreferences preferences = new ReminderPreferences(false, 10, LocalTime.of(23, 30), null);

        List<Reminder> reminders = ReminderPlanner.plan(EVENING, DAY, preferences,
                List.of(task("Bíblia", TODAY, 21, 0)));

        assertThat(reminders).extracting(Reminder::kind).containsExactly(ReminderKind.BEDTIME);
    }

    @Test
    void avisoDeDormirPodeCairNoDiaAnterior() {
        // Dormir à 00:05 com 10 min de antecedência: o aviso sai às 23:55 da véspera
        ReminderPreferences preferences = new ReminderPreferences(true, 10, LocalTime.of(0, 5), null);
        ZonedDateTime lateNight = TODAY.atTime(23, 0).atZone(SAO_PAULO);

        List<Reminder> reminders = ReminderPlanner.plan(lateNight, Duration.ofHours(2), preferences, List.of());

        assertThat(reminders).singleElement().satisfies(reminder -> {
            assertThat(reminder.notifyAt()).isEqualTo(at(TODAY, 23, 55));
            assertThat(reminder.eventAt()).isEqualTo(at(TODAY.plusDays(1), 0, 5));
        });
    }

    @Test
    void semPreferenciasNemTarefasNaoHaLembrete() {
        assertThat(ReminderPlanner.plan(EVENING, DAY, new ReminderPreferences(true, 0, null, null), List.of()))
                .isEmpty();
    }
}
