package com.gasmtask.character.dto;

import java.util.List;

import com.gasmtask.character.domain.CharacterSlot;
import com.gasmtask.character.domain.CharacterState;
import com.gasmtask.store.dto.InventoryItemResponse;

/** @param slots os três slots, sempre presentes; {@code item} nulo é slot vazio */
public record CharacterResponse(CharacterState state, List<Slot> slots) {

    public record Slot(CharacterSlot slot, InventoryItemResponse item) {
    }
}
