package com.gasmtask.store.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.gasmtask.economy.service.WalletService;
import com.gasmtask.shared.exception.BusinessException;
import com.gasmtask.shared.exception.ErrorCode;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.store.domain.InventoryItem;
import com.gasmtask.store.domain.StoreItem;
import com.gasmtask.store.domain.StoreItemCategory;
import com.gasmtask.store.dto.PurchaseResponse;
import com.gasmtask.store.dto.StoreItemResponse;
import com.gasmtask.store.mapper.StoreMapper;
import com.gasmtask.store.repository.InventoryItemRepository;
import com.gasmtask.store.repository.StoreItemRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Catálogo e compra (RN25). */
@Service
public class StoreService {

    private final StoreItemRepository storeItems;
    private final InventoryItemRepository inventory;
    private final WalletService wallet;
    private final UserCalendar calendar;
    private final StoreMapper mapper;
    private final EquipmentService equipment;

    public StoreService(StoreItemRepository storeItems, InventoryItemRepository inventory, WalletService wallet,
                        UserCalendar calendar, StoreMapper mapper, EquipmentService equipment) {
        this.storeItems = storeItems;
        this.inventory = inventory;
        this.wallet = wallet;
        this.calendar = calendar;
        this.mapper = mapper;
        this.equipment = equipment;
    }

    @Transactional(readOnly = true)
    public List<StoreItemResponse> catalog(UUID userId, StoreItemCategory category) {
        List<StoreItem> items = category == null
                ? storeItems.findByAvailableTrueOrderBySortOrderAsc()
                : storeItems.findByAvailableTrueAndCategoryOrderBySortOrderAsc(category);
        Set<UUID> owned = new HashSet<>(inventory.findStoreItemIdsByUserId(userId));
        return items.stream().map(item -> mapper.toResponse(item, owned.contains(item.getId()))).toList();
    }

    /**
     * Débito, lançamento no extrato e entrada no inventário na mesma transação: se faltar saldo, nada fica gravado.
     * A restrição única (usuário, item) impede comprar o mesmo item duas vezes, mesmo com cliques simultâneos.
     */
    @Transactional
    public PurchaseResponse purchase(UUID userId, UUID storeItemId) {
        StoreItem item = storeItems.findById(storeItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Item não encontrado."));
        if (!item.isAvailable()) {
            throw new BusinessException(ErrorCode.ITEM_NOT_AVAILABLE);
        }
        if (inventory.existsByUserIdAndStoreItemId(userId, item.getId())) {
            throw new BusinessException(ErrorCode.ITEM_ALREADY_OWNED);
        }
        if (item.isEquipment()) {
            equipment.checkPurchase(userId, item);
        }
        InventoryItem owned = inventory.save(InventoryItem.acquire(userId, item, calendar.now()));
        wallet.debitPurchase(userId, item.getId(), item.getPrice());
        return new PurchaseResponse(mapper.toResponse(owned, false, null), wallet.balanceOf(userId));
    }
}
