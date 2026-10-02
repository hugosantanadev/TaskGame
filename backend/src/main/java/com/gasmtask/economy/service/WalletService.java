package com.gasmtask.economy.service;

import java.time.Instant;
import java.util.UUID;

import com.gasmtask.economy.domain.CoinTransaction;
import com.gasmtask.economy.domain.CoinTransactionReason;
import com.gasmtask.economy.domain.Wallet;
import com.gasmtask.economy.dto.CoinTransactionResponse;
import com.gasmtask.economy.dto.WalletResponse;
import com.gasmtask.economy.repository.CoinTransactionRepository;
import com.gasmtask.economy.repository.WalletRepository;
import com.gasmtask.shared.exception.BusinessException;
import com.gasmtask.shared.exception.ErrorCode;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.shared.web.PageResponse;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private final WalletRepository wallets;
    private final CoinTransactionRepository transactions;
    private final UserCalendar calendar;

    public WalletService(WalletRepository wallets, CoinTransactionRepository transactions, UserCalendar calendar) {
        this.wallets = wallets;
        this.transactions = transactions;
        this.calendar = calendar;
    }

    /**
     * Paga uma recompensa: lançamento no extrato e crédito no saldo, na mesma transação.
     * Cada motivo é pago no máximo uma vez por ocorrência (RN19); a restrição única no banco garante
     * isso mesmo em requisições simultâneas. Devolve {@code false} se não havia nada a pagar.
     */
    @Transactional
    public boolean creditReward(UUID userId, UUID occurrenceId, CoinTransactionReason reason, int amount) {
        if (amount <= 0 || transactions.existsByOccurrenceIdAndReason(occurrenceId, reason)) {
            return false;
        }
        Instant now = calendar.now();
        transactions.save(CoinTransaction.reward(userId, occurrenceId, reason, amount, now));
        if (wallets.credit(userId, amount, now) == 0) {
            wallets.saveAndFlush(Wallet.open(userId, now));
            wallets.credit(userId, amount, now);
        }
        return true;
    }

    /** Debita uma compra e lança no extrato. Sem saldo, lança INSUFFICIENT_COINS e a transação inteira volta. */
    @Transactional
    public void debitPurchase(UUID userId, UUID storeItemId, int price) {
        Instant now = calendar.now();
        if (wallets.debit(userId, price, now) == 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_COINS);
        }
        transactions.save(CoinTransaction.purchase(userId, storeItemId, price, now));
    }

    @Transactional(readOnly = true)
    public int balanceOf(UUID userId) {
        return wallets.balanceOf(userId).orElse(0);
    }

    @Transactional(readOnly = true)
    public WalletResponse summary(UUID userId) {
        return wallets.findById(userId)
                .map(wallet -> new WalletResponse(wallet.getBalance(), wallet.getTotalEarned(), wallet.getTotalSpent()))
                .orElse(new WalletResponse(0, 0, 0));
    }

    @Transactional(readOnly = true)
    public PageResponse<CoinTransactionResponse> transactions(UUID userId, int page, int size) {
        int safeSize = Math.clamp(size, 1, 100);
        return PageResponse.of(transactions
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(Math.max(page, 0), safeSize))
                .map(tx -> new CoinTransactionResponse(tx.getId(), tx.getAmount(), tx.getReason(),
                        tx.getOccurrenceId(), tx.getStoreItemId(), tx.getCreatedAt())));
    }
}
