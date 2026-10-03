package com.gasmtask.ranking.domain;

/**
 * O que o ranking ordena (RN29). XP é o elo ranqueado (o XP atual, que sobe e desce). Pontos, tarefas e moedas
 * contam só as tarefas concluídas no período; a sequência é a atual, com o dia de hoje somado quando já está
 * cumprido (igual à tela Hoje).
 */
public enum RankingMetric {
    XP, POINTS, COMPLETED_TASKS, COINS_EARNED, STREAK
}
