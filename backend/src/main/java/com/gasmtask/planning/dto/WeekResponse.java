package com.gasmtask.planning.dto;

import java.time.LocalDate;
import java.util.List;

/** @param editable semana atual ou próxima, as únicas que aceitam planejamento */
public record WeekResponse(LocalDate weekStart, LocalDate weekEnd, boolean current, boolean editable,
                           List<DayPlanResponse> days) {
}
