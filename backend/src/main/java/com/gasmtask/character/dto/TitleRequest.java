package com.gasmtask.character.dto;

import com.gasmtask.character.domain.Title;

/** @param title o título a mostrar; nulo tira o título */
public record TitleRequest(Title title) {
}
