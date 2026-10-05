package com.gasmtask.store.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.gasmtask.store.domain.EquipmentTrack;
import com.gasmtask.store.domain.InventoryItem;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {

    boolean existsByUserIdAndStoreItemId(UUID userId, UUID storeItemId);

    @EntityGraph(attributePaths = "storeItem")
    List<InventoryItem> findByUserIdOrderByAcquiredAtDesc(UUID userId);

    @EntityGraph(attributePaths = "storeItem")
    Optional<InventoryItem> findByIdAndUserId(UUID id, UUID userId);

    @Query("select i.storeItem.id from InventoryItem i where i.userId = :userId")
    List<UUID> findStoreItemIdsByUserId(@Param("userId") UUID userId);

    /** Melhorias da pessoa: o maior degrau de cada trilha que ela já tem. */
    @Query("""
            select s.track as track, max(s.tier) as tier
            from InventoryItem i join i.storeItem s
            where i.userId = :userId and s.track is not null
            group by s.track
            """)
    List<TrackTier> findEquipmentTiers(@Param("userId") UUID userId);

    interface TrackTier {
        EquipmentTrack getTrack();

        Integer getTier();
    }
}
