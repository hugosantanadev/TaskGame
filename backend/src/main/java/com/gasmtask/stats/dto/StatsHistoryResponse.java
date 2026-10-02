package com.gasmtask.stats.dto;

import java.time.LocalDate;
import java.util.List;

import com.gasmtask.stats.domain.StatsGranularity;

/** @param periods do mais antigo ao mais recente; o último é o período atual, ainda parcial */
public record StatsHistoryResponse(StatsGranularity granularity, List<Period> periods) {

    public record Period(LocalDate start, LocalDate end, boolean current, TotalsResponse totals) {
    }
}
