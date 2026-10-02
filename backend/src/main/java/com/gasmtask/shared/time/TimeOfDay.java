package com.gasmtask.shared.time;

import java.time.LocalTime;

/** Período do dia no fuso do usuário. Define saudação, cores e, no futuro, a iluminação do quarto. */
public enum TimeOfDay {
    MORNING, AFTERNOON, SUNSET, NIGHT;

    /** Manhã 05:00–11:59, tarde 12:00–16:59, pôr do sol 17:00–18:59, noite 19:00–04:59. */
    public static TimeOfDay of(LocalTime time) {
        int hour = time.getHour();
        if (hour >= 5 && hour < 12) {
            return MORNING;
        }
        if (hour >= 12 && hour < 17) {
            return AFTERNOON;
        }
        if (hour >= 17 && hour < 19) {
            return SUNSET;
        }
        return NIGHT;
    }
}
