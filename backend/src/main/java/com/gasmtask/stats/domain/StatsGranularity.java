package com.gasmtask.stats.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import com.gasmtask.shared.time.UserCalendar;

/** Tamanho do período do histórico. A semana vai de segunda a domingo (RN01); o mês, do dia 1 ao último. */
public enum StatsGranularity {
    WEEK, MONTH;

    public LocalDate startOf(LocalDate date) {
        return switch (this) {
            case WEEK -> UserCalendar.weekStartOf(date);
            case MONTH -> date.withDayOfMonth(1);
        };
    }

    public LocalDate endOf(LocalDate start) {
        return switch (this) {
            case WEEK -> start.plusDays(6);
            case MONTH -> start.with(TemporalAdjusters.lastDayOfMonth());
        };
    }

    /**
     * Início dos últimos {@code count} períodos até o de {@code today} (inclusive), do mais antigo ao mais
     * recente. Não volta para antes do período de {@code since} (o cadastro): antes dele não há o que mostrar.
     */
    public List<LocalDate> lastPeriods(LocalDate today, LocalDate since, int count) {
        LocalDate current = startOf(today);
        LocalDate first = startOf(since);
        long available = unit().between(first, current) + 1;
        int size = (int) Math.max(1, Math.min(count, available));
        List<LocalDate> starts = new ArrayList<>(size);
        for (int back = size - 1; back >= 0; back--) {
            starts.add(current.minus(back, unit()));
        }
        return starts;
    }

    private ChronoUnit unit() {
        return this == WEEK ? ChronoUnit.WEEKS : ChronoUnit.MONTHS;
    }
}
