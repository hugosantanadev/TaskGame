package com.gasmtask.streak.dto;

import java.time.LocalDate;

import com.gasmtask.streak.domain.StreakView;
import com.gasmtask.streak.domain.TodayStatus;

public record StreakResponse(int current, int longest, TodayStatus todayStatus, LocalDate lastFulfilledDate) {

    public static StreakResponse of(StreakView view) {
        return new StreakResponse(view.current(), view.longest(), view.todayStatus(), view.lastFulfilledDate());
    }
}
