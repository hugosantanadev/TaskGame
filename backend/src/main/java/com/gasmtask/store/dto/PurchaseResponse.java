package com.gasmtask.store.dto;

public record PurchaseResponse(InventoryItemResponse inventoryItem, int walletBalance) {
}
