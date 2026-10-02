package com.gasmtask.planning.repository;

import com.gasmtask.task.domain.TaskCategory;

/** Projeção: quantas tarefas concluídas em cada categoria. */
public interface CategoryCount {

    TaskCategory getCategory();

    long getTotal();
}
