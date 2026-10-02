package com.gasmtask.task.domain;

/**
 * O que mudou numa edição de missão.
 *
 * @param planChanged  recorrência, tipo, categoria, pontos, duração ou prova: as ocorrências futuras são refeitas
 * @param titleChanged só o nome: as ocorrências pendentes são renomeadas
 */
public record TaskChange(boolean planChanged, boolean titleChanged) {
}
