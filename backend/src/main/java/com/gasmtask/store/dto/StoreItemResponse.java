package com.gasmtask.store.dto;

import java.util.UUID;

import com.gasmtask.character.domain.CharacterSlot;
import com.gasmtask.store.domain.EquipmentTrack;
import com.gasmtask.store.domain.StoreItemCategory;

/**
 * @param owned se o usuário já tem o item (RF16)
 * @param track trilha da melhoria; nulo nos outros itens
 * @param tier  degrau da melhoria (1 a 3); nulo nos outros itens
 */
public record StoreItemResponse(UUID id, String code, String name, String description, StoreItemCategory category,
                                CharacterSlot slot, int price, String assetKey, boolean owned, EquipmentTrack track,
                                Integer tier) {
}
