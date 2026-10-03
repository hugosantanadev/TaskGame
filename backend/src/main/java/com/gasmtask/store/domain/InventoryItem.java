package com.gasmtask.store.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Posse de um item: quando foi comprado e por quanto (RN25). */
@Entity
@Table(name = "inventory_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryItem {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_item_id", nullable = false, updatable = false)
    private StoreItem storeItem;

    @Column(name = "price_paid", nullable = false, updatable = false)
    private int pricePaid;

    @Column(name = "acquired_at", nullable = false, updatable = false)
    private Instant acquiredAt;

    public static InventoryItem acquire(UUID userId, StoreItem item, Instant now) {
        InventoryItem owned = new InventoryItem();
        owned.id = UUID.randomUUID();
        owned.userId = userId;
        owned.storeItem = item;
        owned.pricePaid = item.getPrice();
        owned.acquiredAt = now;
        return owned;
    }

    /** Recebido sem compra (recompensa de elo): entra na coleção com preço pago zero. */
    public static InventoryItem grant(UUID userId, StoreItem item, Instant now) {
        InventoryItem owned = acquire(userId, item, now);
        owned.pricePaid = 0;
        return owned;
    }
}
