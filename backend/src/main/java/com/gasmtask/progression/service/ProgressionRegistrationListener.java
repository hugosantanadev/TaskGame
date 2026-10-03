package com.gasmtask.progression.service;

import com.gasmtask.progression.domain.PlayerProgress;
import com.gasmtask.progression.repository.PlayerProgressRepository;
import com.gasmtask.user.domain.UserRegisteredEvent;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Toda conta nasce no Ferro 1, com zero de XP. */
@Component
class ProgressionRegistrationListener {

    private final PlayerProgressRepository progress;

    ProgressionRegistrationListener(PlayerProgressRepository progress) {
        this.progress = progress;
    }

    @EventListener
    void onUserRegistered(UserRegisteredEvent event) {
        progress.save(PlayerProgress.start(event.userId(), event.registeredAt()));
    }
}
