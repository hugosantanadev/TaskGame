package com.gasmtask.planning.service;

import com.gasmtask.user.domain.UserRegisteredEvent;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** No cadastro, já cria a semana atual e a próxima (ainda vazias; as missões preenchem). */
@Component
class PlanningRegistrationListener {

    private final PlanningService planning;

    PlanningRegistrationListener(PlanningService planning) {
        this.planning = planning;
    }

    @EventListener
    void onUserRegistered(UserRegisteredEvent event) {
        planning.ensureUpcomingWeeks(event.userId(), event.zone());
    }
}
