package com.gasmtask.economy.domain;

import java.time.Instant;
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

/** Linha do extrato. Imutável: correções viram novos lançamentos, nunca edições. */
@Entity
@Table(name = "coin_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoinTransaction {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, updatable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private CoinTransactionReason reason;

    @Column(name = "occurrence_id", updatable = false)
    private UUID occurrenceId;

    @Column(name = "store_item_id", updatable = false)
    private UUID storeItemId;

    @Column(name = "challenge_id", updatable = false)
    private UUID challengeId;

    @Column(name = "chest_id", updatable = false)
    private UUID chestId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Compra: valor negativo e o item comprado. */
    public static CoinTransaction purchase(UUID userId, UUID storeItemId, int price, Instant now) {
        if (price <= 0) {
            throw new IllegalArgumentException("Preço precisa ser positivo");
        }
        CoinTransaction transaction = new CoinTransaction();
        transaction.id = UUID.randomUUID();
        transaction.userId = userId;
        transaction.storeItemId = storeItemId;
        transaction.reason = CoinTransactionReason.PURCHASE;
        transaction.amount = -price;
        transaction.createdAt = now;
        return transaction;
    }

    /** Compra de um protetor de sequência: valor negativo, sem item da loja. */
    public static CoinTransaction streakFreeze(UUID userId, int price, Instant now) {
        if (price <= 0) {
            throw new IllegalArgumentException("Preço precisa ser positivo");
        }
        CoinTransaction transaction = new CoinTransaction();
        transaction.id = UUID.randomUUID();
        transaction.userId = userId;
        transaction.reason = CoinTransactionReason.STREAK_FREEZE;
        transaction.amount = -price;
        transaction.createdAt = now;
        return transaction;
    }

    /** Moedas de um baú semanal aberto. */
    public static CoinTransaction chestReward(UUID userId, UUID chestId, int amount, Instant now) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Recompensa precisa ser positiva");
        }
        CoinTransaction transaction = new CoinTransaction();
        transaction.id = UUID.randomUUID();
        transaction.userId = userId;
        transaction.chestId = chestId;
        transaction.reason = CoinTransactionReason.CHEST_REWARD;
        transaction.amount = amount;
        transaction.createdAt = now;
        return transaction;
    }

    /** Recompensa de um desafio diário cumprido. */
    public static CoinTransaction challengeReward(UUID userId, UUID challengeId, int amount, Instant now) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Recompensa precisa ser positiva");
        }
        CoinTransaction transaction = new CoinTransaction();
        transaction.id = UUID.randomUUID();
        transaction.userId = userId;
        transaction.challengeId = challengeId;
        transaction.reason = CoinTransactionReason.CHALLENGE_REWARD;
        transaction.amount = amount;
        transaction.createdAt = now;
        return transaction;
    }

    public static CoinTransaction reward(UUID userId, UUID occurrenceId, CoinTransactionReason reason, int amount,
                                         Instant now) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Recompensa precisa ser positiva");
        }
        CoinTransaction transaction = new CoinTransaction();
        transaction.id = UUID.randomUUID();
        transaction.userId = userId;
        transaction.occurrenceId = occurrenceId;
        transaction.reason = reason;
        transaction.amount = amount;
        transaction.createdAt = now;
        return transaction;
    }
}
