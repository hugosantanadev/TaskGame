package com.gasmtask.progression.dto;

import java.util.List;

import com.gasmtask.store.dto.StoreItemResponse;

/**
 * O que uma ação fez com o XP.
 *
 * @param gained        quanto mudou (negativo numa perda)
 * @param promoted      subiu de degrau (divisão ou elo)
 * @param demoted       caiu de degrau
 * @param unlockedItems roupas de elo entregues agora
 */
public record XpChangeResponse(int gained, RankStatusResponse status, boolean promoted, boolean demoted,
                               List<StoreItemResponse> unlockedItems) {
}
