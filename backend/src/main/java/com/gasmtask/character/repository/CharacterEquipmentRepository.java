package com.gasmtask.character.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.gasmtask.character.domain.CharacterEquipment;
import com.gasmtask.character.domain.CharacterSlot;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterEquipmentRepository extends JpaRepository<CharacterEquipment, UUID> {

    @EntityGraph(attributePaths = {"inventoryItem", "inventoryItem.storeItem"})
    List<CharacterEquipment> findByCharacterId(UUID characterId);

    Optional<CharacterEquipment> findByCharacterIdAndSlot(UUID characterId, CharacterSlot slot);
}
