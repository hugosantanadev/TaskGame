package com.gasmtask.store.controller;

import java.util.List;
import java.util.UUID;

import com.gasmtask.shared.security.AuthenticatedUser;
import com.gasmtask.store.domain.StoreItemCategory;
import com.gasmtask.store.dto.EquipmentTrackResponse;
import com.gasmtask.store.dto.InventoryItemResponse;
import com.gasmtask.store.dto.PurchaseResponse;
import com.gasmtask.store.dto.StoreItemResponse;
import com.gasmtask.store.service.EquipmentService;
import com.gasmtask.store.service.InventoryService;
import com.gasmtask.store.service.StoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Loja e inventário")
public class StoreController {

    private final StoreService storeService;
    private final InventoryService inventoryService;
    private final EquipmentService equipmentService;

    public StoreController(StoreService storeService, InventoryService inventoryService,
                           EquipmentService equipmentService) {
        this.storeService = storeService;
        this.inventoryService = inventoryService;
        this.equipmentService = equipmentService;
    }

    @GetMapping("/equipment")
    @Operation(summary = "Melhorias do quarto: o degrau de cada trilha, o próximo à venda e o bônus em moedas")
    public List<EquipmentTrackResponse> equipment(@AuthenticationPrincipal AuthenticatedUser user) {
        return equipmentService.overview(user.id());
    }

    @GetMapping("/store/items")
    @Operation(summary = "Catálogo (filtro opcional por categoria), marcando o que o usuário já tem")
    public List<StoreItemResponse> catalog(@AuthenticationPrincipal AuthenticatedUser user,
                                           @RequestParam(required = false) StoreItemCategory category) {
        return storeService.catalog(user.id(), category);
    }

    @PostMapping("/store/items/{id}/purchase")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Compra com moedas: débito, extrato e inventário na mesma transação")
    public PurchaseResponse purchase(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
        return storeService.purchase(user.id(), id);
    }

    @GetMapping("/inventory")
    @Operation(summary = "Itens do usuário e onde estão em uso")
    public List<InventoryItemResponse> inventory(@AuthenticationPrincipal AuthenticatedUser user) {
        return inventoryService.list(user.id());
    }
}
