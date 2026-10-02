package com.gasmtask.stats.dto;

import com.gasmtask.stats.domain.PeriodTotals;

/**
 * Planejado × concluído de um período.
 *
 * @param completionRate concluídas sobre planejadas, em porcentagem inteira; nulo quando nada foi planejado
 */
public record TotalsResponse(int mandatoryPlanned, int mandatoryDone, int extrasPlanned, int extrasDone,
                             int planned, int done, int missed, int points, int coins, Integer completionRate) {

    public static TotalsResponse of(PeriodTotals totals) {
        return new TotalsResponse(totals.mandatoryPlanned(), totals.mandatoryDone(), totals.extrasPlanned(),
                totals.extrasDone(), totals.planned(), totals.done(), totals.missed(), totals.points(),
                totals.coins(), totals.completionRate());
    }
}
