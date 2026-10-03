package com.gasmtask.character.domain;

import java.util.Arrays;
import java.util.Set;

import com.gasmtask.task.domain.TaskCategory;

/**
 * Atributos do personagem, como num RPG: cada categoria de tarefa treina exatamente um atributo.
 * Treino só soma (quem sobe e desce é o elo).
 */
public enum Attribute {
    INTELLIGENCE(TaskCategory.STUDY),
    STRENGTH(TaskCategory.EXERCISE),
    WISDOM(TaskCategory.READING),
    SPIRIT(TaskCategory.SPIRITUALITY),
    VITALITY(TaskCategory.SLEEP),
    CREATIVITY(TaskCategory.PROJECT),
    DISCIPLINE(TaskCategory.HOME, TaskCategory.OTHER);

    private final Set<TaskCategory> categories;

    Attribute(TaskCategory... categories) {
        this.categories = Set.of(categories);
    }

    public Set<TaskCategory> categories() {
        return categories;
    }

    public static Attribute of(TaskCategory category) {
        return Arrays.stream(values())
                .filter(attribute -> attribute.categories.contains(category))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Categoria sem atributo: " + category));
    }
}
