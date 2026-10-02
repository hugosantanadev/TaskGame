package com.gasmtask.character.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * O personagem do usuário (a tabela é "characters"; a classe não se chama Character para não colidir com
 * java.lang.Character). Não grava estado: o que ele está fazendo é derivado na leitura (RN05).
 */
@Entity
@Table(name = "characters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerCharacter {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true, updatable = false)
    private UUID userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static PlayerCharacter create(UUID userId, Instant now) {
        PlayerCharacter character = new PlayerCharacter();
        character.id = UUID.randomUUID();
        character.userId = userId;
        character.createdAt = now;
        return character;
    }
}
