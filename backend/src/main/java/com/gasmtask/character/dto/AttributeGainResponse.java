package com.gasmtask.character.dto;

import java.util.List;

import com.gasmtask.character.domain.Title;

/**
 * O treino que uma conclusão deu: quanto o atributo ganhou e se subiu de nível.
 *
 * @param status         o atributo depois do ganho
 * @param unlockedTitles títulos ganhos agora (ex.: "Mestre nos estudos" no nível 10 de Inteligência)
 */
public record AttributeGainResponse(int gained, boolean leveledUp, AttributeResponse status,
                                    List<Title> unlockedTitles) {
}
