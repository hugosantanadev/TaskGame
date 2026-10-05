package com.gasmtask.store.dto;

import java.util.List;
import java.util.Set;

import com.gasmtask.store.domain.EquipmentTrack;
import com.gasmtask.task.domain.TaskCategory;

/**
 * Uma trilha de melhoria do quarto.
 *
 * @param tier      degrau atual (0 = o quarto de quem está começando)
 * @param coinBonus moedas a mais por tarefa das categorias da trilha, no degrau atual
 * @param current   item do degrau atual; nulo no degrau zero
 * @param next      próximo degrau à venda; nulo quando a trilha está completa
 * @param nextBonus bônus com o próximo degrau
 * @param steps     os três degraus, do primeiro ao último
 */
public record EquipmentTrackResponse(EquipmentTrack track, Set<TaskCategory> categories, int tier, int coinBonus,
                                     StoreItemResponse current, StoreItemResponse next, Integer nextBonus,
                                     List<StoreItemResponse> steps) {
}
