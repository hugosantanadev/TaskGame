package com.gasmtask.shared.time;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

import org.springframework.stereotype.Component;

/**
 * "Agora", "hoje" e "semana" sempre no fuso do usuário (RN01), a partir do relógio único da aplicação.
 * A semana vai de segunda a domingo.
 */
@Component
public class UserCalendar {

    private final Clock clock;

    public UserCalendar(Clock clock) {
        this.clock = clock;
    }

    public Instant now() {
        return clock.instant();
    }

    public ZonedDateTime now(ZoneId zone) {
        return ZonedDateTime.ofInstant(clock.instant(), zone);
    }

    public LocalDate today(ZoneId zone) {
        return LocalDate.ofInstant(clock.instant(), zone);
    }

    public static LocalDate weekStartOf(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDate localDateOf(Instant instant, ZoneId zone) {
        return LocalDate.ofInstant(instant, zone);
    }
}
