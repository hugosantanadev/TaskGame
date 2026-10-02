package com.gasmtask.room.domain;

import java.time.Instant;
import java.util.UUID;

import com.gasmtask.store.domain.InventoryItem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Item do inventário colocado no quarto. Posição e camada chegam com a camada visual. */
@Entity
@Table(name = "room_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomItem {

    @Id
    private UUID id;

    @Column(name = "room_id", nullable = false, updatable = false)
    private UUID roomId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_item_id", nullable = false, unique = true, updatable = false)
    private InventoryItem inventoryItem;

    @Column(name = "placed_at", nullable = false, updatable = false)
    private Instant placedAt;

    public static RoomItem place(Room room, InventoryItem item, Instant now) {
        if (!item.getStoreItem().isRoomItem()) {
            throw new IllegalArgumentException("Só móveis e decoração vão para o quarto");
        }
        RoomItem placed = new RoomItem();
        placed.id = UUID.randomUUID();
        placed.roomId = room.getId();
        placed.inventoryItem = item;
        placed.placedAt = now;
        return placed;
    }
}
