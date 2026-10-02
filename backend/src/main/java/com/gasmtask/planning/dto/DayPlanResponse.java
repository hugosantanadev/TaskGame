package com.gasmtask.planning.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * @param canAddMandatory se dá para incluir obrigatórias nesse dia agora
 * @param canAddExtra     se dá para criar extras nesse dia agora
 */
public record DayPlanResponse(
        LocalDate date,
        DayOfWeek dayOfWeek,
        boolean today,
        boolean past,
        boolean canAddMandatory,
        boolean canAddExtra,
        List<OccurrenceResponse> occurrences) {
}
