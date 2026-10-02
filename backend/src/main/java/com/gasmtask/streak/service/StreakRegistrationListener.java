package com.gasmtask.streak.service;

import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.streak.domain.Streak;
import com.gasmtask.streak.repository.StreakRepository;
import com.gasmtask.user.domain.UserRegisteredEvent;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** O streak começa a contar no dia do cadastro, no fuso do usuário. */
@Component
class StreakRegistrationListener {

    private final StreakRepository streaks;

    StreakRegistrationListener(StreakRepository streaks) {
        this.streaks = streaks;
    }

    @EventListener
    void onUserRegistered(UserRegisteredEvent event) {
        streaks.save(Streak.start(event.userId(), UserCalendar.localDateOf(event.registeredAt(), event.zone())));
    }
}
