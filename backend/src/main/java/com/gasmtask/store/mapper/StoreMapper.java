package com.gasmtask.store.mapper;

import com.gasmtask.character.domain.CharacterSlot;
import com.gasmtask.store.domain.InventoryItem;
import com.gasmtask.store.domain.StoreItem;
import com.gasmtask.store.dto.InventoryItemResponse;
import com.gasmtask.store.dto.StoreItemResponse;

import org.springframework.stereotype.Component;

@Component
public class StoreMapper {

    public StoreItemResponse toResponse(StoreItem item, boolean owned) {
        return new StoreItemResponse(item.getId(), item.getCode(), item.getName(), item.getDescription(),
                item.getCategory(), item.getSlot(), item.getPrice(), item.getAssetKey(), owned);
    }

    public InventoryItemResponse toResponse(InventoryItem owned, boolean inRoom, CharacterSlot equippedSlot) {
        return new InventoryItemResponse(owned.getId(), toResponse(owned.getStoreItem(), true), owned.getPricePaid(),
                owned.getAcquiredAt(), inRoom, equippedSlot);
    }
}
