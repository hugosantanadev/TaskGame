package com.gasmtask.store.service;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.gasmtask.shared.exception.BusinessException;
import com.gasmtask.shared.exception.ErrorCode;
import com.gasmtask.store.domain.EquipmentTrack;
import com.gasmtask.store.domain.StoreItem;
import com.gasmtask.store.domain.StoreItemCategory;
import com.gasmtask.store.dto.EquipmentTrackResponse;
import com.gasmtask.store.dto.StoreItemResponse;
import com.gasmtask.store.mapper.StoreMapper;
import com.gasmtask.store.repository.InventoryItemRepository;
import com.gasmtask.store.repository.StoreItemRepository;
import com.gasmtask.task.domain.TaskCategory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Melhorias do quarto: em que degrau está cada trilha, o que vem depois e o bônus que cada uma rende. */
@Service
public class EquipmentService {

    private final InventoryItemRepository inventory;
    private final StoreItemRepository storeItems;
    private final StoreMapper mapper;

    public EquipmentService(InventoryItemRepository inventory, StoreItemRepository storeItems, StoreMapper mapper) {
        this.inventory = inventory;
        this.storeItems = storeItems;
        this.mapper = mapper;
    }

    /** O degrau de cada trilha; quem não comprou nada da trilha está no zero. */
    @Transactional(readOnly = true)
    public Map<EquipmentTrack, Integer> tiers(UUID userId) {
        Map<EquipmentTrack, Integer> tiers = new EnumMap<>(EquipmentTrack.class);
        Arrays.stream(EquipmentTrack.values()).forEach(track -> tiers.put(track, 0));
        inventory.findEquipmentTiers(userId).forEach(row -> tiers.put(row.getTrack(), row.getTier()));
        return tiers;
    }

    /** Moedas a mais que uma tarefa da categoria rende com as melhorias de agora. */
    @Transactional(readOnly = true)
    public int coinBonus(UUID userId, TaskCategory category) {
        return EquipmentTrack.forCategory(category)
                .map(track -> EquipmentTrack.coinBonus(tiers(userId).get(track)))
                .orElse(0);
    }

    /**
     * Uma melhoria só pode ser comprada sobre o degrau anterior: quem tem a escrivaninha não compra a mesa
     * dobrável, e quem está no chão não pula direto para a escrivaninha em L.
     */
    @Transactional(readOnly = true)
    public void checkPurchase(UUID userId, StoreItem item) {
        int current = tiers(userId).get(item.getTrack());
        if (item.getTier() <= current) {
            throw new BusinessException(ErrorCode.ITEM_ALREADY_OWNED, "Você já tem essa melhoria ou uma melhor.");
        }
        if (item.getTier() > current + 1) {
            throw new BusinessException(ErrorCode.EQUIPMENT_LOCKED);
        }
    }

    @Transactional(readOnly = true)
    public List<EquipmentTrackResponse> overview(UUID userId) {
        Map<EquipmentTrack, Integer> tiers = tiers(userId);
        Set<UUID> owned = new HashSet<>(inventory.findStoreItemIdsByUserId(userId));
        List<StoreItem> catalog = storeItems.findByCategoryOrderBySortOrderAsc(StoreItemCategory.EQUIPMENT);
        return Arrays.stream(EquipmentTrack.values()).map(track -> {
            int tier = tiers.get(track);
            List<StoreItemResponse> steps = catalog.stream()
                    .filter(item -> item.getTrack() == track)
                    .map(item -> mapper.toResponse(item, owned.contains(item.getId())))
                    .toList();
            StoreItemResponse current = steps.stream().filter(step -> step.tier() == tier).findFirst().orElse(null);
            StoreItemResponse next = steps.stream()
                    .filter(step -> step.tier() == tier + 1)
                    .findFirst()
                    .orElse(null);
            return new EquipmentTrackResponse(track, track.categories(), tier, EquipmentTrack.coinBonus(tier),
                    current, next, next == null ? null : EquipmentTrack.coinBonus(next.tier()), steps);
        }).toList();
    }
}
