package com.gasmtask.streak.domain;

import java.time.LocalDate;

/** Streak como o usuário vê: dias fechados mais o dia de hoje, se já cumprido. */
public record StreakView(int current, int longest, TodayStatus todayStatus, LocalDate lastFulfilledDate) {
}
