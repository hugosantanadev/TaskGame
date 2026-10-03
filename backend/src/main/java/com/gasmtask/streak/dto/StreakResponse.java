package com.gasmtask.streak.dto;

import java.time.LocalDate;

import com.gasmtask.streak.config.StreakProperties;
import com.gasmtask.streak.domain.StreakView;
import com.gasmtask.streak.domain.TodayStatus;

/**
 * @param freezes        protetores de sequência guardados, até {@code maxFreezes}
 * @param freezePrice    quanto custa mais um protetor, em moedas
 * @param lastFrozenDate último dia que um protetor salvou
 */
public record StreakResponse(int current, int longest, TodayStatus todayStatus, LocalDate lastFulfilledDate,
                             int freezes, int maxFreezes, int freezePrice, LocalDate lastFrozenDate) {

    public static StreakResponse of(StreakView view, StreakProperties properties) {
        return new StreakResponse(view.current(), view.longest(), view.todayStatus(), view.lastFulfilledDate(),
                view.freezes(), properties.maxFreezes(), properties.freezePrice(), view.lastFrozenDate());
    }
}
