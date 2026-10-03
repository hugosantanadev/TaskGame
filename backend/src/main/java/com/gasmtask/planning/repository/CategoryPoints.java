package com.gasmtask.planning.repository;

import com.gasmtask.task.domain.TaskCategory;

/** Projeção: pontos obtidos com as tarefas concluídas de cada categoria. */
public interface CategoryPoints {

    TaskCategory getCategory();

    long getPoints();
}
