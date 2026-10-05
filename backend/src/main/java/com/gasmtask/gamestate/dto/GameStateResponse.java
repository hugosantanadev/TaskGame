package com.gasmtask.gamestate.dto;

import java.util.List;
import java.util.Map;

import com.gasmtask.character.domain.Attribute;
import com.gasmtask.character.domain.CharacterSlot;
import com.gasmtask.character.domain.CharacterState;
import com.gasmtask.progression.dto.RankStatusResponse;
import com.gasmtask.shared.time.TimeOfDay;
import com.gasmtask.store.domain.EquipmentTrack;
import com.gasmtask.streak.domain.TodayStatus;

/**
 * Tudo o que a futura camada visual precisa para desenhar o quarto e o personagem (RF24), numa chamada.
 * Os itens vêm pelo código estável e pela {@code assetKey} lógica: quem resolve a chave em sprite é a camada
 * visual, não o domínio.
 *
 * @param timeOfDay período do dia no fuso do usuário (iluminação do quarto)
 * @param coins     saldo atual
 */
public record GameStateResponse(
        TimeOfDay timeOfDay,
        int coins,
        StreakState streak,
        RankStatusResponse rank,
        Totals totals,
        List<Item> inventory,
        RoomState room,
        CharacterLook character) {

    public record StreakState(int current, int longest, TodayStatus todayStatus) {
    }

    public record Totals(int completedTasks, int achievements) {
    }

    /** @param assetKey chave lógica do visual (ex.: {@code furniture.bookshelf.v1}); pode ser nula */
    public record Item(String code, String assetKey) {
    }

    /**
     * @param items     decoração colocada à mão
     * @param equipment degrau de cada trilha de melhoria (0 = o quarto de quem está começando)
     */
    public record RoomState(List<Item> items, Map<EquipmentTrack, Integer> equipment) {
    }

    /**
     * @param equipped   só os slots ocupados
     * @param attributes nível de cada atributo (Força, Inteligência...)
     */
    public record CharacterLook(CharacterState state, Map<CharacterSlot, Item> equipped,
                                Map<Attribute, Integer> attributes) {
    }
}
