package com.gasmtask.challenge.dto;

import com.gasmtask.challenge.domain.ChallengeType;

/**
 * Um desafio do dia. O texto é do app, a partir do código e da meta.
 *
 * @param progress quanto já foi feito, limitado à meta
 */
public record DailyChallengeResponse(ChallengeType code, int target, int progress, boolean completed, int xpReward,
                                     int coinReward) {
}
