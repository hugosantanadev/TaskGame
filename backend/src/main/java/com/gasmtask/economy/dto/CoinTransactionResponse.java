package com.gasmtask.economy.dto;

import java.time.Instant;
import java.util.UUID;

import com.gasmtask.economy.domain.CoinTransactionReason;

/** Valor negativo é compra ({@code storeItemId} diz o item); positivo é recompensa de uma tarefa. */
public record CoinTransactionResponse(UUID id, int amount, CoinTransactionReason reason, UUID occurrenceId,
                                      UUID storeItemId, Instant createdAt) {
}
