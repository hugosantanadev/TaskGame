package com.gasmtask.store.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.gasmtask.character.domain.CharacterSlot;
import com.gasmtask.character.service.CharacterService;
import com.gasmtask.room.service.RoomService;
import com.gasmtask.store.dto.InventoryItemResponse;
import com.gasmtask.store.mapper.StoreMapper;
import com.gasmtask.store.repository.InventoryItemRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** A coleção do usuário, dizendo onde cada item está em uso (quarto ou personagem). */
@Service
public class InventoryService {

    private final InventoryItemRepository inventory;
    private final RoomService rooms;
    private final CharacterService characters;
    private final StoreMapper mapper;

    public InventoryService(InventoryItemRepository inventory, RoomService rooms, CharacterService characters,
                            StoreMapper mapper) {
        this.inventory = inventory;
        this.rooms = rooms;
        this.characters = characters;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> list(UUID userId) {
        Set<UUID> inRoom = rooms.placedInventoryIds(userId);
        Map<UUID, CharacterSlot> equipped = characters.equippedSlots(userId);
        return inventory.findByUserIdOrderByAcquiredAtDesc(userId).stream()
                .map(owned -> mapper.toResponse(owned, inRoom.contains(owned.getId()), equipped.get(owned.getId())))
                .toList();
    }
}
