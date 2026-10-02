package com.gasmtask.character.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record EquipRequest(@NotNull UUID inventoryItemId) {
}
