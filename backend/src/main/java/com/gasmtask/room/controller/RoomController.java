package com.gasmtask.room.controller;

import java.util.UUID;

import com.gasmtask.room.dto.RoomResponse;
import com.gasmtask.room.service.RoomService;
import com.gasmtask.shared.security.AuthenticatedUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/room")
@Tag(name = "Quarto")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    @Operation(summary = "Itens colocados no quarto")
    public RoomResponse room(@AuthenticationPrincipal AuthenticatedUser user) {
        return roomService.room(user.id());
    }

    @PutMapping("/items/{inventoryItemId}")
    @Operation(summary = "Coloca um móvel ou decoração da coleção no quarto (idempotente)")
    public RoomResponse place(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID inventoryItemId) {
        return roomService.place(user.id(), inventoryItemId);
    }

    @DeleteMapping("/items/{inventoryItemId}")
    @Operation(summary = "Tira o item do quarto; ele continua na coleção")
    public RoomResponse remove(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID inventoryItemId) {
        return roomService.remove(user.id(), inventoryItemId);
    }
}
