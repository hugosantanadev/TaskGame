package com.gasmtask.ranking.domain;

/**
 * Janela do ranking (RN29). No MVP só a semana atual, de segunda a domingo, no fuso de quem consulta.
 * O critério é modelado como (período, métrica, escopo) para que mês e outros escopos entrem como novos valores.
 */
public enum RankingPeriod {
    WEEK
}
