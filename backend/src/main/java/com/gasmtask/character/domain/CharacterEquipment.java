package com.gasmtask.character.domain;

import java.time.Instant;
import java.util.UUID;

import com.gasmtask.store.domain.InventoryItem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Item vestido num slot. Trocar o item do slot reaproveita a linha, então nunca há dois itens no mesmo slot. */
@Entity
@Table(name = "character_equipment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CharacterEquipment {

    @Id
    private UUID id;

    @Column(name = "character_id", nullable = false, updatable = false)
    private UUID characterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    private CharacterSlot slot;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_item_id", nullable = false, unique = true)
    private InventoryItem inventoryItem;

    @Column(name = "equipped_at", nullable = false)
    private Instant equippedAt;

    public static CharacterEquipment equip(PlayerCharacter character, CharacterSlot slot, InventoryItem item, Instant now) {
        CharacterEquipment equipment = new CharacterEquipment();
        equipment.id = UUID.randomUUID();
        equipment.characterId = character.getId();
        equipment.slot = slot;
        equipment.swap(item, now);
        return equipment;
    }

    public void swap(InventoryItem item, Instant now) {
        if (!item.getStoreItem().fitsSlot(slot)) {
            throw new IllegalArgumentException("O item " + item.getStoreItem().getCode() + " não serve no slot " + slot);
        }
        this.inventoryItem = item;
        this.equippedAt = now;
    }
}
