package com.gasmtask.character.repository;

import java.util.Optional;
import java.util.UUID;

import com.gasmtask.character.domain.PlayerCharacter;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerCharacterRepository extends JpaRepository<PlayerCharacter, UUID> {

    Optional<PlayerCharacter> findByUserId(UUID userId);
}
