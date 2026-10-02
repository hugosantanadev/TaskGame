package com.gasmtask.store.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import com.gasmtask.character.domain.CharacterEquipment;
import com.gasmtask.character.domain.CharacterSlot;
import com.gasmtask.character.domain.PlayerCharacter;
import com.gasmtask.room.domain.Room;
import com.gasmtask.room.domain.RoomItem;

import org.junit.jupiter.api.Test;

/** RN25 e RN26: o que vai para o quarto, o que vai para o personagem e em qual slot. */
class CollectionRulesTest {

    private static final Instant NOW = Instant.parse("2026-09-28T12:00:00Z");

    private final UUID userId = UUID.randomUUID();
    private final StoreItem bookshelf = StoreItem.of("bookshelf_wood", "Estante", StoreItemCategory.FURNITURE, null, 80);
    private final StoreItem plant = StoreItem.of("plant_small", "Suculenta", StoreItemCategory.DECORATION, null, 8);
    private final StoreItem cap = StoreItem.of("cap_red", "Boné", StoreItemCategory.CHARACTER, CharacterSlot.HEAD, 12);
    private final StoreItem hoodie = StoreItem.of("hoodie_purple", "Moletom", StoreItemCategory.CHARACTER, CharacterSlot.OUTFIT, 70);

    @Test
    void moveisEDecoracaoVaoParaOQuarto() {
        assertThat(bookshelf.isRoomItem()).isTrue();
        assertThat(plant.isRoomItem()).isTrue();
        assertThat(cap.isRoomItem()).isFalse();
    }

    @Test
    void itemDePersonagemSoServeNoProprioSlot() {
        assertThat(cap.fitsSlot(CharacterSlot.HEAD)).isTrue();
        assertThat(cap.fitsSlot(CharacterSlot.OUTFIT)).isFalse();
        assertThat(bookshelf.fitsSlot(CharacterSlot.HEAD)).isFalse();
    }

    @Test
    void slotExisteSoEmItemDePersonagem() {
        assertThatThrownBy(() -> StoreItem.of("x", "x", StoreItemCategory.FURNITURE, CharacterSlot.HEAD, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StoreItem.of("y", "y", StoreItemCategory.CHARACTER, null, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void compraGuardaOPrecoPago() {
        assertThat(InventoryItem.acquire(userId, cap, NOW).getPricePaid()).isEqualTo(12);
    }

    @Test
    void quartoRecusaItemDePersonagem() {
        Room room = Room.create(userId, NOW);

        assertThat(RoomItem.place(room, InventoryItem.acquire(userId, plant, NOW), NOW).getRoomId()).isEqualTo(room.getId());
        assertThatThrownBy(() -> RoomItem.place(room, InventoryItem.acquire(userId, cap, NOW), NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void slotTrocaOItemMasSoPorOutroDoMesmoSlot() {
        PlayerCharacter character = PlayerCharacter.create(userId, NOW);
        CharacterEquipment head = CharacterEquipment.equip(character, CharacterSlot.HEAD,
                InventoryItem.acquire(userId, cap, NOW), NOW);
        StoreItem headphones = StoreItem.of("headphones_basic", "Fones", StoreItemCategory.CHARACTER, CharacterSlot.HEAD, 50);
        InventoryItem ownedHeadphones = InventoryItem.acquire(userId, headphones, NOW);

        head.swap(ownedHeadphones, NOW);

        assertThat(head.getInventoryItem()).isEqualTo(ownedHeadphones);
        assertThatThrownBy(() -> head.swap(InventoryItem.acquire(userId, hoodie, NOW), NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
