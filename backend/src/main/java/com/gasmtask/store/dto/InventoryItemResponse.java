package com.gasmtask.store.dto;

import java.time.Instant;
import java.util.UUID;

import com.gasmtask.character.domain.CharacterSlot;

/**
 * Um item da coleção e onde está em uso.
 *
 * @param id           id do item no inventário (é ele que vai para o quarto ou para o personagem)
 * @param equippedSlot slot do personagem em que está vestido, ou nulo
 */
public record InventoryItemResponse(UUID id, StoreItemResponse item, int pricePaid, Instant acquiredAt,
                                    boolean inRoom, CharacterSlot equippedSlot) {
}
