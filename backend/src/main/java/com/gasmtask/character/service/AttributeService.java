package com.gasmtask.character.service;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.gasmtask.character.domain.Attribute;
import com.gasmtask.character.domain.AttributeLevels;
import com.gasmtask.character.dto.AttributeGainResponse;
import com.gasmtask.character.dto.AttributeResponse;
import com.gasmtask.planning.repository.CategoryPoints;
import com.gasmtask.planning.repository.TaskOccurrenceRepository;
import com.gasmtask.task.domain.TaskCategory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Atributos do personagem, derivados das tarefas concluídas: o XP de cada atributo é a soma dos pontos das
 * categorias que o treinam. Nada é gravado; editar o histórico é impossível, então a conta é sempre a mesma.
 */
@Service
public class AttributeService {

    private final TaskOccurrenceRepository occurrences;

    public AttributeService(TaskOccurrenceRepository occurrences) {
        this.occurrences = occurrences;
    }

    @Transactional(readOnly = true)
    public List<AttributeResponse> attributes(UUID userId) {
        Map<Attribute, Integer> xp = xpByAttribute(userId);
        return Arrays.stream(Attribute.values())
                .map(attribute -> AttributeResponse.of(attribute, xp.get(attribute)))
                .toList();
    }

    /** Nível de cada atributo, para a camada visual. */
    @Transactional(readOnly = true)
    public Map<Attribute, Integer> levels(UUID userId) {
        Map<Attribute, Integer> levels = new EnumMap<>(Attribute.class);
        xpByAttribute(userId).forEach((attribute, xp) -> levels.put(attribute, AttributeLevels.levelOf(xp)));
        return levels;
    }

    /** O treino de uma tarefa recém-concluída (já contada na soma) da categoria dada. */
    @Transactional(readOnly = true)
    public AttributeGainResponse gainFrom(UUID userId, TaskCategory category, int points) {
        Attribute attribute = Attribute.of(category);
        int after = xpByAttribute(userId).get(attribute);
        int before = Math.max(0, after - points);
        return new AttributeGainResponse(points, AttributeLevels.levelOf(after) > AttributeLevels.levelOf(before),
                AttributeResponse.of(attribute, after));
    }

    private Map<Attribute, Integer> xpByAttribute(UUID userId) {
        Map<Attribute, Integer> xp = new EnumMap<>(Attribute.class);
        for (Attribute attribute : Attribute.values()) {
            xp.put(attribute, 0);
        }
        for (CategoryPoints points : occurrences.sumPointsByCategory(userId)) {
            xp.merge(Attribute.of(points.getCategory()), (int) points.getPoints(), Integer::sum);
        }
        return xp;
    }
}
