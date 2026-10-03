package com.gasmtask.character.service;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.gasmtask.character.domain.CharacterEquipment;
import com.gasmtask.character.domain.CharacterSlot;
import com.gasmtask.character.domain.CharacterState;
import com.gasmtask.character.domain.CharacterStateResolver;
import com.gasmtask.character.domain.PlayerCharacter;
import com.gasmtask.character.dto.CharacterResponse;
import com.gasmtask.character.repository.CharacterEquipmentRepository;
import com.gasmtask.character.repository.PlayerCharacterRepository;
import com.gasmtask.planning.repository.TaskOccurrenceRepository;
import com.gasmtask.shared.exception.BusinessException;
import com.gasmtask.shared.exception.ErrorCode;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.store.domain.InventoryItem;
import com.gasmtask.store.mapper.StoreMapper;
import com.gasmtask.store.repository.InventoryItemRepository;
import com.gasmtask.user.service.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Slots do personagem (RN26) e o estado derivado da tarefa em andamento (RN05). */
@Service
public class CharacterService {

    private final PlayerCharacterRepository characters;
    private final CharacterEquipmentRepository equipment;
    private final InventoryItemRepository inventory;
    private final TaskOccurrenceRepository occurrences;
    private final UserService users;
    private final UserCalendar calendar;
    private final StoreMapper mapper;
    private final AttributeService attributes;

    public CharacterService(PlayerCharacterRepository characters, CharacterEquipmentRepository equipment,
                            InventoryItemRepository inventory, TaskOccurrenceRepository occurrences, UserService users,
                            UserCalendar calendar, StoreMapper mapper, AttributeService attributes) {
        this.characters = characters;
        this.equipment = equipment;
        this.inventory = inventory;
        this.occurrences = occurrences;
        this.users = users;
        this.calendar = calendar;
        this.mapper = mapper;
        this.attributes = attributes;
    }

    @Transactional
    public CharacterResponse view(UUID userId) {
        PlayerCharacter character = characterOf(userId);
        Map<CharacterSlot, CharacterEquipment> bySlot = equipment.findByCharacterId(character.getId()).stream()
                .collect(Collectors.toMap(CharacterEquipment::getSlot, Function.identity()));
        ZonedDateTime now = calendar.now(users.timeInfo(userId).zone());
        CharacterState state = CharacterStateResolver.resolve(
                occurrences.findByUserIdAndOccurrenceDate(userId, now.toLocalDate()), now.toLocalTime());
        return new CharacterResponse(state, Arrays.stream(CharacterSlot.values())
                .map(slot -> new CharacterResponse.Slot(slot, bySlot.containsKey(slot)
                        ? mapper.toResponse(bySlot.get(slot).getInventoryItem(), false, slot)
                        : null))
                .toList(), attributes.attributes(userId));
    }

    @Transactional
    public CharacterResponse equip(UUID userId, CharacterSlot slot, UUID inventoryItemId) {
        InventoryItem item = inventory.findByIdAndUserId(inventoryItemId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Item não está na sua coleção."));
        if (!item.getStoreItem().fitsSlot(slot)) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOR_SLOT);
        }
        PlayerCharacter character = characterOf(userId);
        equipment.findByCharacterIdAndSlot(character.getId(), slot).ifPresentOrElse(
                current -> current.swap(item, calendar.now()),
                () -> equipment.save(CharacterEquipment.equip(character, slot, item, calendar.now())));
        return view(userId);
    }

    @Transactional
    public CharacterResponse unequip(UUID userId, CharacterSlot slot) {
        PlayerCharacter character = characterOf(userId);
        equipment.findByCharacterIdAndSlot(character.getId(), slot).ifPresent(equipment::delete);
        return view(userId);
    }

    /** Itens vestidos: id do item no inventário → slot. */
    @Transactional(readOnly = true)
    public Map<UUID, CharacterSlot> equippedSlots(UUID userId) {
        return characters.findByUserId(userId)
                .map(character -> equipment.findByCharacterId(character.getId()).stream()
                        .collect(Collectors.toMap(entry -> entry.getInventoryItem().getId(), CharacterEquipment::getSlot)))
                .orElse(Map.of());
    }

    private PlayerCharacter characterOf(UUID userId) {
        return characters.findByUserId(userId)
                .orElseGet(() -> characters.save(PlayerCharacter.create(userId, calendar.now())));
    }
}
