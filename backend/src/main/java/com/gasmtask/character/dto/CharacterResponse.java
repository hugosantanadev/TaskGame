package com.gasmtask.character.dto;

import java.util.List;

import com.gasmtask.character.domain.CharacterSlot;
import com.gasmtask.character.domain.CharacterState;
import com.gasmtask.store.dto.InventoryItemResponse;

/**
 * @param slots      os três slots, sempre presentes; {@code item} nulo é slot vazio
 * @param attributes a ficha: um nível por atributo, treinado pelas tarefas concluídas
 * @param titles     todos os títulos, dizendo quais já foram ganhos
 */
public record CharacterResponse(CharacterState state, List<Slot> slots, List<AttributeResponse> attributes,
                                List<TitleResponse> titles, String activeTitle) {

    public record Slot(CharacterSlot slot, InventoryItemResponse item) {
    }
}
