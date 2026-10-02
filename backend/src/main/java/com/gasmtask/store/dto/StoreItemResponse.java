package com.gasmtask.store.dto;

import java.util.UUID;

import com.gasmtask.character.domain.CharacterSlot;
import com.gasmtask.store.domain.StoreItemCategory;

/** @param owned se o usuário já tem o item (RF16) */
public record StoreItemResponse(UUID id, String code, String name, String description, StoreItemCategory category,
                                CharacterSlot slot, int price, String assetKey, boolean owned) {
}
