package com.gasmtask.economy.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.gasmtask.economy.domain.Wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    /** Crédito atômico; devolve 0 se a carteira não existe. O flush antes grava o lançamento do extrato. */
    @Modifying(flushAutomatically = true)
    @Query("""
            update Wallet w set w.balance = w.balance + :amount, w.totalEarned = w.totalEarned + :amount,
                w.updatedAt = :now
            where w.userId = :userId
            """)
    int credit(@Param("userId") UUID userId, @Param("amount") int amount, @Param("now") Instant now);

    /** Débito atômico e condicional: devolve 0 se o saldo não cobre o valor, e nada muda (RN21). */
    @Modifying(flushAutomatically = true)
    @Query("""
            update Wallet w set w.balance = w.balance - :amount, w.totalSpent = w.totalSpent + :amount,
                w.updatedAt = :now
            where w.userId = :userId and w.balance >= :amount
            """)
    int debit(@Param("userId") UUID userId, @Param("amount") int amount, @Param("now") Instant now);

    @Query("select w.balance from Wallet w where w.userId = :userId")
    Optional<Integer> balanceOf(@Param("userId") UUID userId);
}
