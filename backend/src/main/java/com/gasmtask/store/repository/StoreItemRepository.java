package com.gasmtask.store.repository;

import java.util.List;
import java.util.UUID;

import com.gasmtask.store.domain.StoreItem;
import com.gasmtask.store.domain.StoreItemCategory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreItemRepository extends JpaRepository<StoreItem, UUID> {

    List<StoreItem> findByAvailableTrueOrderBySortOrderAsc();

    List<StoreItem> findByAvailableTrueAndCategoryOrderBySortOrderAsc(StoreItemCategory category);
}
