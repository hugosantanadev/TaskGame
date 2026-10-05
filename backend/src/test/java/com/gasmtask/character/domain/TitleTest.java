package com.gasmtask.character.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;

class TitleTest {

    @Test
    void cadaAtributoTemTresTitulosNosNiveis3610() {
        for (Attribute attribute : Attribute.values()) {
            assertThat(Arrays.stream(Title.values()).filter(title -> title.attribute() == attribute))
                    .extracting(Title::level).containsExactly(3, 6, 10);
        }
    }

    @Test
    void tituloSaiQuandoONivelPassaDoLimite() {
        assertThat(Title.reachedBetween(Attribute.STRENGTH, 2, 3)).containsExactly(Title.GYM_REGULAR);
        assertThat(Title.reachedBetween(Attribute.STRENGTH, 3, 4)).isEmpty();
        assertThat(Title.reachedBetween(Attribute.INTELLIGENCE, 5, 10))
                .containsExactly(Title.SHARP_MIND, Title.STUDY_MASTER);

        Map<Attribute, Integer> levels = Map.of(Attribute.INTELLIGENCE, 10, Attribute.STRENGTH, 4);
        assertThat(Title.STUDY_MASTER.unlockedBy(levels)).isTrue();
        assertThat(Title.GYM_RAT.unlockedBy(levels)).isFalse();
        assertThat(Title.CURIOUS_READER.unlockedBy(levels)).isFalse(); // atributo sem treino: nível 1
    }
}
