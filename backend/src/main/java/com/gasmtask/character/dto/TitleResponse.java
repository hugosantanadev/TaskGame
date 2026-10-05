package com.gasmtask.character.dto;

import com.gasmtask.character.domain.Attribute;
import com.gasmtask.character.domain.Title;

/** Um título da ficha: de qual atributo, em que nível, e se já foi ganho. */
public record TitleResponse(Title code, Attribute attribute, int level, boolean unlocked) {
}
