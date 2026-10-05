package com.gasmtask.store.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;

import com.gasmtask.task.domain.TaskCategory;

import org.junit.jupiter.api.Test;

class EquipmentTrackTest {

    @Test
    void todaCategoriaTemExatamenteUmaTrilha() {
        for (TaskCategory category : TaskCategory.values()) {
            long tracks = Arrays.stream(EquipmentTrack.values())
                    .filter(track -> track.categories().contains(category))
                    .count();
            assertThat(tracks).as(category.name()).isEqualTo(1);
        }
        assertThat(EquipmentTrack.forCategory(TaskCategory.EXERCISE)).contains(EquipmentTrack.GYM);
        assertThat(EquipmentTrack.forCategory(TaskCategory.OTHER)).contains(EquipmentTrack.ORGANIZER);
    }

    @Test
    void cadaDegrauRendeUmaMoedaAMais() {
        assertThat(EquipmentTrack.coinBonus(0)).isZero();
        assertThat(EquipmentTrack.coinBonus(1)).isEqualTo(1);
        assertThat(EquipmentTrack.coinBonus(3)).isEqualTo(3);
        assertThatThrownBy(() -> EquipmentTrack.coinBonus(4)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void melhoriaNaoVaiParaOQuartoAMao() {
        StoreItem desk = StoreItem.equipment("desk_folding", "Mesa dobrável", EquipmentTrack.DESK, 1, 25);
        assertThat(desk.isEquipment()).isTrue();
        assertThat(desk.isRoomItem()).isFalse();
        assertThatThrownBy(() -> StoreItem.equipment("x", "x", EquipmentTrack.DESK, 0, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
