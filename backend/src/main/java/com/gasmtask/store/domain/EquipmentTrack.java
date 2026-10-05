package com.gasmtask.store.domain;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import com.gasmtask.task.domain.TaskCategory;

/**
 * Trilhas de melhoria do quarto. Cada uma tem três degraus à venda (o degrau zero é o quarto de quem está
 * começando) e rende moedas a mais nas tarefas das categorias dela: +1 por degrau. Toda categoria tem
 * exatamente uma trilha.
 */
public enum EquipmentTrack {
    COMPUTER(EnumSet.of(TaskCategory.PROJECT)),
    DESK(EnumSet.of(TaskCategory.STUDY)),
    BED(EnumSet.of(TaskCategory.SLEEP)),
    BOOKSHELF(EnumSet.of(TaskCategory.READING)),
    GYM(EnumSet.of(TaskCategory.EXERCISE)),
    PEACE(EnumSet.of(TaskCategory.SPIRITUALITY)),
    ORGANIZER(EnumSet.of(TaskCategory.HOME, TaskCategory.OTHER));

    public static final int MAX_TIER = 3;

    private final Set<TaskCategory> categories;

    EquipmentTrack(Set<TaskCategory> categories) {
        this.categories = categories;
    }

    public Set<TaskCategory> categories() {
        return categories;
    }

    public static Optional<EquipmentTrack> forCategory(TaskCategory category) {
        return Arrays.stream(values()).filter(track -> track.categories.contains(category)).findFirst();
    }

    /** Moedas a mais por tarefa concluída da categoria, no degrau dado (zero no degrau inicial). */
    public static int coinBonus(int tier) {
        if (tier < 0 || tier > MAX_TIER) {
            throw new IllegalArgumentException("Degrau fora da trilha: " + tier);
        }
        return tier;
    }
}
