package com.gasmtask.planning.repository;

import java.time.LocalDate;

import com.gasmtask.planning.domain.OccurrenceStatus;
import com.gasmtask.task.domain.TaskKind;

/** Projeção: tarefas de um dia agrupadas por tipo e situação, com os pontos e moedas obtidos. */
public interface DailyCount {

    LocalDate getDate();

    TaskKind getKind();

    OccurrenceStatus getStatus();

    long getTotal();

    long getPoints();

    long getCoins();
}
