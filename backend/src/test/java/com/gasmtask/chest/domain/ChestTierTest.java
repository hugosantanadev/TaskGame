package com.gasmtask.chest.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ChestTierTest {

    @Test
    void baúCresceComOsDiasCumpridos() {
        assertThat(ChestTier.forFulfilledDays(0)).isEqualTo(ChestTier.NONE);
        assertThat(ChestTier.forFulfilledDays(1)).isEqualTo(ChestTier.NONE);
        assertThat(ChestTier.forFulfilledDays(3)).isEqualTo(ChestTier.WOOD);
        assertThat(ChestTier.forFulfilledDays(5)).isEqualTo(ChestTier.SILVER);
        assertThat(ChestTier.forFulfilledDays(6)).isEqualTo(ChestTier.GOLD);
        assertThat(ChestTier.forFulfilledDays(7)).isEqualTo(ChestTier.LEGENDARY);
    }

    @Test
    void soOuroELendarioTrazemItem() {
        LocalDate monday = LocalDate.of(2026, 9, 28);
        Instant now = Instant.parse("2026-10-05T12:00:00Z");
        UUID user = UUID.randomUUID();

        assertThat(WeeklyChest.of(user, monday, 3, "plant_small", now).getItemCode()).isNull();
        WeeklyChest gold = WeeklyChest.of(user, monday, 6, "plant_small", now);
        assertThat(gold.getItemCode()).isEqualTo("plant_small");
        assertThat(gold.getCoins()).isEqualTo(50);
        assertThat(gold.hasReward()).isTrue();
        assertThat(WeeklyChest.of(user, monday, 1, null, now).hasReward()).isFalse();
    }
}
