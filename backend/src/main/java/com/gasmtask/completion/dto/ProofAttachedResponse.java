package com.gasmtask.completion.dto;

import java.util.List;

import com.gasmtask.challenge.dto.DailyChallengeResponse;
import com.gasmtask.planning.dto.OccurrenceResponse;

/**
 * @param proofBonus          moedas pagas agora pela prova (0 se o bônus já tinha sido pago)
 * @param completedChallenges desafios do dia cumpridos com esta foto
 */
public record ProofAttachedResponse(OccurrenceResponse occurrence, int proofBonus, int walletBalance,
                                    List<DailyChallengeResponse> completedChallenges) {
}
