package com.gasmtask.chest.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * O baú de uma semana. Nasce fechado, com o conteúdo já decidido (moedas, XP e, às vezes, um item), e é aberto
 * uma vez. Semanas sem baú (tier NONE) também ficam registradas, para não recalcular toda vez.
 */
@Entity
@Table(name = "weekly_chests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyChest {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "week_start", nullable = false, updatable = false)
    private LocalDate weekStart;

    @Column(name = "fulfilled_days", nullable = false, updatable = false)
    private int fulfilledDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    private ChestTier tier;

    @Column(nullable = false, updatable = false)
    private int coins;

    @Column(nullable = false, updatable = false)
    private int xp;

    @Column(name = "item_code", length = 40, updatable = false)
    private String itemCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "opened_at")
    private Instant openedAt;

    public static WeeklyChest of(UUID userId, LocalDate weekStart, int fulfilledDays, String itemCode, Instant now) {
        ChestTier tier = ChestTier.forFulfilledDays(fulfilledDays);
        WeeklyChest chest = new WeeklyChest();
        chest.id = UUID.randomUUID();
        chest.userId = userId;
        chest.weekStart = weekStart;
        chest.fulfilledDays = fulfilledDays;
        chest.tier = tier;
        chest.coins = tier.coins();
        chest.xp = tier.xp();
        chest.itemCode = tier.maxItemPrice() > 0 ? itemCode : null;
        chest.createdAt = now;
        return chest;
    }

    public boolean isOpened() {
        return openedAt != null;
    }

    public boolean hasReward() {
        return tier != ChestTier.NONE;
    }

    public void open(Instant now) {
        if (isOpened()) {
            throw new IllegalStateException("Baú já aberto");
        }
        openedAt = now;
    }
}
