package com.gasmtask.completion.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Evidência de uma conclusão. O arquivo fica no {@code FileStorage}; aqui só a referência e os metadados. */
@Entity
@Table(name = "proofs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Proof {

    @Id
    private UUID id;

    @Column(name = "occurrence_id", nullable = false, updatable = false, unique = true)
    private UUID occurrenceId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, length = 10, updatable = false)
    private String kind;

    @Column(name = "storage_key", nullable = false, length = 200, updatable = false)
    private String storageKey;

    @Column(name = "content_type", nullable = false, length = 50, updatable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false, updatable = false)
    private int sizeBytes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static Proof image(UUID occurrenceId, UUID userId, String storageKey, String contentType, int sizeBytes,
                              Instant now) {
        Proof proof = new Proof();
        proof.id = UUID.randomUUID();
        proof.occurrenceId = occurrenceId;
        proof.userId = userId;
        proof.kind = "IMAGE";
        proof.storageKey = storageKey;
        proof.contentType = contentType;
        proof.sizeBytes = sizeBytes;
        proof.createdAt = now;
        return proof;
    }
}
