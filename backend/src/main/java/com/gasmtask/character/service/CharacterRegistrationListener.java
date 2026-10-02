package com.gasmtask.character.service;

import com.gasmtask.character.domain.PlayerCharacter;
import com.gasmtask.character.repository.PlayerCharacterRepository;
import com.gasmtask.user.domain.UserRegisteredEvent;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class CharacterRegistrationListener {

    private final PlayerCharacterRepository characters;

    CharacterRegistrationListener(PlayerCharacterRepository characters) {
        this.characters = characters;
    }

    @EventListener
    void onUserRegistered(UserRegisteredEvent event) {
        characters.save(PlayerCharacter.create(event.userId(), event.registeredAt()));
    }
}
