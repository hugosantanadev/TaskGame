package com.gasmtask.room.service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gasmtask.room.domain.Room;
import com.gasmtask.room.domain.RoomItem;
import com.gasmtask.room.dto.RoomResponse;
import com.gasmtask.room.repository.RoomItemRepository;
import com.gasmtask.room.repository.RoomRepository;
import com.gasmtask.shared.exception.BusinessException;
import com.gasmtask.shared.exception.ErrorCode;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.store.domain.InventoryItem;
import com.gasmtask.store.mapper.StoreMapper;
import com.gasmtask.store.repository.InventoryItemRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RN26: só itens do inventário, e só móveis e decoração, vão para o quarto. Colocar de novo não duplica. */
@Service
public class RoomService {

    private final RoomRepository rooms;
    private final RoomItemRepository roomItems;
    private final InventoryItemRepository inventory;
    private final UserCalendar calendar;
    private final StoreMapper mapper;

    public RoomService(RoomRepository rooms, RoomItemRepository roomItems, InventoryItemRepository inventory,
                       UserCalendar calendar, StoreMapper mapper) {
        this.rooms = rooms;
        this.roomItems = roomItems;
        this.inventory = inventory;
        this.calendar = calendar;
        this.mapper = mapper;
    }

    @Transactional
    public RoomResponse room(UUID userId) {
        Room room = roomOf(userId);
        return new RoomResponse(roomItems.findByRoomIdOrderByPlacedAtAsc(room.getId()).stream()
                .map(placed -> mapper.toResponse(placed.getInventoryItem(), true, null))
                .toList());
    }

    @Transactional
    public RoomResponse place(UUID userId, UUID inventoryItemId) {
        InventoryItem item = owned(userId, inventoryItemId);
        if (item.getStoreItem().isEquipment()) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOR_ROOM, "Melhorias aparecem no quarto sozinhas.");
        }
        if (!item.getStoreItem().isRoomItem()) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOR_ROOM);
        }
        if (!roomItems.existsByInventoryItemId(item.getId())) {
            roomItems.save(RoomItem.place(roomOf(userId), item, calendar.now()));
        }
        return room(userId);
    }

    @Transactional
    public RoomResponse remove(UUID userId, UUID inventoryItemId) {
        InventoryItem item = owned(userId, inventoryItemId);
        roomItems.findByInventoryItemId(item.getId()).ifPresent(roomItems::delete);
        return room(userId);
    }

    @Transactional(readOnly = true)
    public Set<UUID> placedInventoryIds(UUID userId) {
        return rooms.findByUserId(userId)
                .map(room -> roomItems.findByRoomIdOrderByPlacedAtAsc(room.getId()).stream()
                        .map(placed -> placed.getInventoryItem().getId())
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    private InventoryItem owned(UUID userId, UUID inventoryItemId) {
        return inventory.findByIdAndUserId(inventoryItemId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Item não está na sua coleção."));
    }

    private Room roomOf(UUID userId) {
        return rooms.findByUserId(userId).orElseGet(() -> rooms.save(Room.create(userId, calendar.now())));
    }
}
