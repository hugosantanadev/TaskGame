package com.gasmtask.economy.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Saldo de moedas (RN18). Créditos e débitos usam UPDATE atômico no repositório, então duas conclusões
 * simultâneas nunca perdem moedas; o banco garante que o saldo não fica negativo (RN21).
 */
@Entity
@Table(name = "wallets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private int balance;

    @Column(name = "total_earned", nullable = false)
    private int totalEarned;

    @Column(name = "total_spent", nullable = false)
    private int totalSpent;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public static Wallet open(UUID userId, Instant now) {
        Wallet wallet = new Wallet();
        wallet.userId = userId;
        wallet.updatedAt = now;
        return wallet;
    }
}
