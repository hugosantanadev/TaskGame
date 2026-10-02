package com.gasmtask.completion.dto;

import com.gasmtask.planning.dto.OccurrenceResponse;

/** @param proofBonus moedas pagas agora pela prova (0 se o bônus já tinha sido pago) */
public record ProofAttachedResponse(OccurrenceResponse occurrence, int proofBonus, int walletBalance) {
}
