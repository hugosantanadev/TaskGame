package com.gasmtask.store.domain;

import java.util.UUID;

import com.gasmtask.character.domain.CharacterSlot;

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
 * Item do catálogo da loja. O catálogo é seed de migration; o {@code code} é estável e a {@code assetKey}
 * é a chave lógica que a futura camada visual vai transformar em sprite.
 */
@Entity
@Table(name = "store_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreItem {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 40, updatable = false)
    private String code;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(nullable = false, length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StoreItemCategory category;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private CharacterSlot slot;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private boolean available;

    @Column(name = "asset_key", length = 60)
    private String assetKey;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    /** O catálogo nasce na migration; esta fábrica serve a testes. */
    public static StoreItem of(String code, String name, StoreItemCategory category, CharacterSlot slot, int price) {
        if ((category == StoreItemCategory.CHARACTER) != (slot != null)) {
            throw new IllegalArgumentException("Só itens de personagem têm slot");
        }
        StoreItem item = new StoreItem();
        item.id = UUID.randomUUID();
        item.code = code;
        item.name = name;
        item.description = name;
        item.category = category;
        item.slot = slot;
        item.price = price;
        item.available = true;
        return item;
    }

    public boolean isRoomItem() {
        return category == StoreItemCategory.FURNITURE || category == StoreItemCategory.DECORATION;
    }

    public boolean fitsSlot(CharacterSlot target) {
        return category == StoreItemCategory.CHARACTER && slot == target;
    }
}
