package com.gasmtask.ranking.dto;

import java.time.LocalDate;

import com.gasmtask.progression.dto.RankResponse;
import com.gasmtask.ranking.domain.RankingMetric;
import com.gasmtask.ranking.domain.RankingPeriod;
import com.gasmtask.ranking.domain.RankingScope;
import com.gasmtask.shared.web.PageResponse;

/**
 * Ranking de um período (RF20). Só sai o nome de exibição e o valor de cada pessoa (RNF11): nenhum id.
 *
 * @param entries quem escolheu aparecer e pontuou, em ordem; {@code you} marca a linha de quem consulta
 * @param me      o placar de quem consulta, mesmo que não apareça na lista
 */
public record RankingResponse(
        RankingPeriod period,
        RankingMetric metric,
        RankingScope scope,
        LocalDate from,
        LocalDate to,
        PageResponse<Entry> entries,
        Me me) {

    /** @param rank o elo da pessoa (pelo XP atual), em qualquer métrica */
    public record Entry(int position, String displayName, int value, RankResponse rank, boolean you) {
    }

    /**
     * @param position onde a pessoa está (ou estaria, se estiver oculta) entre os visíveis; nulo sem pontuação
     * @param visible  escolheu aparecer no ranking (perfil)
     */
    public record Me(Integer position, int value, RankResponse rank, boolean visible) {
    }
}
