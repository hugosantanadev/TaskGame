package com.gasmtask.ranking.domain;

/**
 * O que o ranking ordena (RN29). As três primeiras contam só tarefas concluídas no período;
 * a sequência é a atual, com o dia de hoje somado quando já está cumprido (igual à tela Hoje).
 */
public enum RankingMetric {
    POINTS, COMPLETED_TASKS, COINS_EARNED, STREAK
}
