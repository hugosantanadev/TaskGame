package com.gasmtask.room.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** O ambiente do usuário (um quarto no MVP). Nasce fino: a camada visual vai acrescentar tema e layout. */
@Entity
@Table(name = "rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true, updatable = false)
    private UUID userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static Room create(UUID userId, Instant now) {
        Room room = new Room();
        room.id = UUID.randomUUID();
        room.userId = userId;
        room.createdAt = now;
        return room;
    }
}
