package com.gasmtask.room.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.gasmtask.room.domain.RoomItem;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomItemRepository extends JpaRepository<RoomItem, UUID> {

    @EntityGraph(attributePaths = {"inventoryItem", "inventoryItem.storeItem"})
    List<RoomItem> findByRoomIdOrderByPlacedAtAsc(UUID roomId);

    Optional<RoomItem> findByInventoryItemId(UUID inventoryItemId);

    boolean existsByInventoryItemId(UUID inventoryItemId);
}
