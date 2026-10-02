package com.gasmtask.planning.config;

import com.gasmtask.task.domain.TaskKind;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** RN12: teto diário de tarefas por tipo. */
@ConfigurationProperties("app.planning")
public record PlanningProperties(int maxMandatoryPerDay, int maxExtrasPerDay) {

    public PlanningProperties {
        if (maxMandatoryPerDay < 1 || maxExtrasPerDay < 1) {
            throw new IllegalArgumentException("Os tetos diários de app.planning precisam ser positivos");
        }
    }

    public int limitFor(TaskKind kind) {
        return kind == TaskKind.MANDATORY ? maxMandatoryPerDay : maxExtrasPerDay;
    }
}
