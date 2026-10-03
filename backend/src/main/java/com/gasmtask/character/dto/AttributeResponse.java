package com.gasmtask.character.dto;

import java.util.Set;

import com.gasmtask.character.domain.Attribute;
import com.gasmtask.character.domain.AttributeLevels;
import com.gasmtask.task.domain.TaskCategory;

/**
 * Um atributo na ficha: nível e a barra até o próximo ({@code levelStartXp} a {@code nextLevelXp}).
 *
 * @param categories as categorias de tarefa que treinam este atributo
 */
public record AttributeResponse(Attribute attribute, int xp, int level, int levelStartXp, int nextLevelXp,
                                Set<TaskCategory> categories) {

    public static AttributeResponse of(Attribute attribute, int xp) {
        int level = AttributeLevels.levelOf(xp);
        return new AttributeResponse(attribute, xp, level, AttributeLevels.startOf(level),
                AttributeLevels.startOf(level + 1), attribute.categories());
    }
}
