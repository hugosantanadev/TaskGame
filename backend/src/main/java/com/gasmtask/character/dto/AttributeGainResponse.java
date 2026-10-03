package com.gasmtask.character.dto;

/**
 * O treino que uma conclusão deu: quanto o atributo ganhou e se subiu de nível.
 *
 * @param status o atributo depois do ganho
 */
public record AttributeGainResponse(int gained, boolean leveledUp, AttributeResponse status) {
}
