package com.gasmtask.user.domain;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Preferências de lembrete (RF03), embutidas no usuário. Imutável: mudar é trocar o objeto inteiro.
 * Horários de dormir e de acordar são opcionais; sem eles, não há lembrete desse tipo.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReminderSettings {

    public static final int MAX_LEAD_MINUTES = 120;
    static final int DEFAULT_LEAD_MINUTES = 10;

    @Column(name = "reminder_tasks_enabled", nullable = false)
    private boolean tasksEnabled;

    /** Quantos minutos antes do horário da tarefa (ou da hora de dormir) o aviso sai. */
    @Column(name = "reminder_lead_minutes", nullable = false)
    private int leadMinutes;

    @Column(name = "bedtime")
    private LocalTime bedtime;

    @Column(name = "wake_time")
    private LocalTime wakeTime;

    public static ReminderSettings defaults() {
        return of(true, DEFAULT_LEAD_MINUTES, null, null);
    }

    public static ReminderSettings of(boolean tasksEnabled, int leadMinutes, LocalTime bedtime, LocalTime wakeTime) {
        if (leadMinutes < 0 || leadMinutes > MAX_LEAD_MINUTES) {
            throw new IllegalArgumentException("Antecedência fora da faixa: " + leadMinutes);
        }
        ReminderSettings settings = new ReminderSettings();
        settings.tasksEnabled = tasksEnabled;
        settings.leadMinutes = leadMinutes;
        settings.bedtime = bedtime;
        settings.wakeTime = wakeTime;
        return settings;
    }
}
