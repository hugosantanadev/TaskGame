package com.gasmtask.store.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gasmtask.character.domain.CharacterSlot;
import com.gasmtask.character.service.CharacterService;
import com.gasmtask.room.service.RoomService;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.store.domain.InventoryItem;
import com.gasmtask.store.domain.StoreItem;
import com.gasmtask.store.dto.InventoryItemResponse;
import com.gasmtask.store.dto.StoreItemResponse;
import com.gasmtask.store.mapper.StoreMapper;
import com.gasmtask.store.repository.InventoryItemRepository;
import com.gasmtask.store.repository.StoreItemRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** A coleção do usuário, dizendo onde cada item está em uso (quarto ou personagem). */
@Service
public class InventoryService {

    private final InventoryItemRepository inventory;
    private final StoreItemRepository storeItems;
    private final RoomService rooms;
    private final CharacterService characters;
    private final UserCalendar calendar;
    private final StoreMapper mapper;

    public InventoryService(InventoryItemRepository inventory, StoreItemRepository storeItems, RoomService rooms,
                            CharacterService characters, UserCalendar calendar, StoreMapper mapper) {
        this.inventory = inventory;
        this.storeItems = storeItems;
        this.rooms = rooms;
        this.characters = characters;
        this.calendar = calendar;
        this.mapper = mapper;
    }

    /**
     * Entrega itens sem compra (recompensas). Idempotente: o que a pessoa já tem fica como está.
     * Devolve só os itens entregues agora, na ordem pedida.
     */
    @Transactional
    public List<StoreItemResponse> grant(UUID userId, List<String> codes) {
        if (codes.isEmpty()) {
            return List.of();
        }
        Set<UUID> owned = new HashSet<>(inventory.findStoreItemIdsByUserId(userId));
        Map<String, StoreItem> byCode = storeItems.findByCodeIn(codes).stream()
                .collect(Collectors.toMap(StoreItem::getCode, item -> item));
        return codes.stream()
                .map(byCode::get)
                .filter(item -> item != null && !owned.contains(item.getId()))
                .map(item -> {
                    inventory.save(InventoryItem.grant(userId, item, calendar.now()));
                    return mapper.toResponse(item, true);
                })
                .toList();
    }

    /** Itens do catálogo pelo código (inclusive os fora da loja), dizendo se a pessoa já tem cada um. */
    @Transactional(readOnly = true)
    public List<StoreItemResponse> itemsByCodes(UUID userId, Collection<String> codes) {
        Set<UUID> owned = new HashSet<>(inventory.findStoreItemIdsByUserId(userId));
        return storeItems.findByCodeIn(codes).stream()
                .map(item -> mapper.toResponse(item, owned.contains(item.getId())))
                .toList();
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
