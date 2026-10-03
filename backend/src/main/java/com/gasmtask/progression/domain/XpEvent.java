package com.gasmtask.progression.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Linha do extrato de XP. Imutável. Restrições únicas no banco garantem que cada tarefa renda (ou custe)
 * XP uma vez e que cada dia cumprido dê o bônus uma vez, mesmo com requisições simultâneas.
 */
@Entity
@Table(name = "xp_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class XpEvent {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, updatable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private XpReason reason;

    @Column(name = "occurrence_id", updatable = false)
    private UUID occurrenceId;

    @Column(name = "event_date", updatable = false)
    private LocalDate eventDate;

    @Column(name = "challenge_id", updatable = false)
    private UUID challengeId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static XpEvent of(UUID userId, int amount, XpReason reason, UUID occurrenceId, LocalDate eventDate,
                             UUID challengeId, Instant now) {
        if (amount == 0) {
            throw new IllegalArgumentException("Evento de XP precisa mudar alguma coisa");
        }
        XpEvent event = new XpEvent();
        event.id = UUID.randomUUID();
        event.userId = userId;
        event.amount = amount;
        event.reason = reason;
        event.occurrenceId = occurrenceId;
        event.eventDate = eventDate;
        event.challengeId = challengeId;
        event.createdAt = now;
        return event;
    }
}
