package com.gasmtask.streak.service;

import java.util.UUID;

import com.gasmtask.planning.service.PlanningService;
import com.gasmtask.user.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Percorre os usuários periodicamente: fecha os dias que já viraram no fuso de cada um e garante
 * a semana atual e a próxima. Cada usuário roda na própria transação; a falha de um não para os outros.
 */
@Component
@ConditionalOnProperty(prefix = "app.day-closing", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DayClosingJob {

    private static final Logger log = LoggerFactory.getLogger(DayClosingJob.class);

    private final UserService users;
    private final DayClosingService closing;
    private final PlanningService planning;

    public DayClosingJob(UserService users, DayClosingService closing, PlanningService planning) {
        this.users = users;
        this.closing = closing;
        this.planning = planning;
    }

    @Scheduled(fixedDelayString = "${app.day-closing.interval}", initialDelayString = "${app.day-closing.initial-delay}")
    public void run() {
        for (UUID userId : users.allIds()) {
            try {
                closing.closePendingDays(userId);
                planning.ensureUpcomingWeeks(userId);
            } catch (RuntimeException e) {
                log.warn("Falha ao fechar os dias do usuário {}", userId, e);
            }
        }
    }
}
