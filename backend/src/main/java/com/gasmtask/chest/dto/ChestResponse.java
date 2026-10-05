package com.gasmtask.chest.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.gasmtask.chest.domain.ChestTier;
import com.gasmtask.chest.domain.WeeklyChest;

/**
 * Um baú semanal. O conteúdo exato (qual item) só aparece ao abrir.
 *
 * @param fulfilledDays dias cumpridos na semana que o baú premia
 * @param hasItem       traz um item da loja
 */
public record ChestResponse(UUID id, LocalDate weekStart, ChestTier tier, int fulfilledDays, int coins, int xp,
                            boolean hasItem, boolean opened) {

    public static ChestResponse of(WeeklyChest chest) {
        return new ChestResponse(chest.getId(), chest.getWeekStart(), chest.getTier(), chest.getFulfilledDays(),
                chest.getCoins(), chest.getXp(), chest.getItemCode() != null, chest.isOpened());
    }
}
