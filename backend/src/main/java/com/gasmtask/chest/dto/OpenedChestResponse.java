package com.gasmtask.chest.dto;

import com.gasmtask.progression.dto.XpChangeResponse;
import com.gasmtask.store.dto.StoreItemResponse;

/**
 * O que saiu do baú.
 *
 * @param coins moedas pagas (com as do item, se a pessoa já tinha o item)
 * @param item  item entregue na coleção; nulo se o baú não trazia ou se virou moedas
 */
public record OpenedChestResponse(ChestResponse chest, int coins, StoreItemResponse item, XpChangeResponse xp,
                                  int walletBalance) {
}
