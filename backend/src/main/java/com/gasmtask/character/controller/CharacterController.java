package com.gasmtask.character.controller;

import com.gasmtask.character.domain.CharacterSlot;
import com.gasmtask.character.dto.CharacterResponse;
import com.gasmtask.character.dto.EquipRequest;
import com.gasmtask.character.service.CharacterService;
import com.gasmtask.shared.security.AuthenticatedUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/character")
@Tag(name = "Personagem")
public class CharacterController {

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @GetMapping
    @Operation(summary = "Slots vestidos e o estado derivado da tarefa em andamento")
    public CharacterResponse character(@AuthenticationPrincipal AuthenticatedUser user) {
        return characterService.view(user.id());
    }

    @PutMapping("/slots/{slot}")
    @Operation(summary = "Veste um item da coleção no slot (troca o que estava lá)")
    public CharacterResponse equip(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable CharacterSlot slot,
                                   @Valid @RequestBody EquipRequest request) {
        return characterService.equip(user.id(), slot, request.inventoryItemId());
    }

    @DeleteMapping("/slots/{slot}")
    @Operation(summary = "Esvazia o slot; o item continua na coleção")
    public CharacterResponse unequip(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable CharacterSlot slot) {
        return characterService.unequip(user.id(), slot);
    }
}
