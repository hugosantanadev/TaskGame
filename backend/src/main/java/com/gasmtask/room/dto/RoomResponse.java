package com.gasmtask.room.dto;

import java.util.List;

import com.gasmtask.store.dto.InventoryItemResponse;

public record RoomResponse(List<InventoryItemResponse> items) {
}
