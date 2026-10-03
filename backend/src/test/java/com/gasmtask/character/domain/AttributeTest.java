package com.gasmtask.character.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import com.gasmtask.task.domain.TaskCategory;

import org.junit.jupiter.api.Test;

class AttributeTest {

    @Test
    void cadaCategoriaTreinaExatamenteUmAtributo() {
        for (TaskCategory category : TaskCategory.values()) {
            long trainers = Arrays.stream(Attribute.values())
                    .filter(attribute -> attribute.categories().contains(category))
                    .count();
            assertThat(trainers).as(category.name()).isEqualTo(1);
        }
        assertThat(Attribute.of(TaskCategory.EXERCISE)).isEqualTo(Attribute.STRENGTH);
        assertThat(Attribute.of(TaskCategory.OTHER)).isEqualTo(Attribute.DISCIPLINE);
    }

    @Test
    void cadaNivelPede25XpAMaisQueOAnterior() {
        assertThat(AttributeLevels.startOf(1)).isZero();
        assertThat(AttributeLevels.startOf(2)).isEqualTo(25);
        assertThat(AttributeLevels.startOf(3)).isEqualTo(75);
        assertThat(AttributeLevels.startOf(5)).isEqualTo(250);
        assertThat(AttributeLevels.startOf(10)).isEqualTo(1125);

        assertThat(AttributeLevels.levelOf(0)).isEqualTo(1);
        assertThat(AttributeLevels.levelOf(24)).isEqualTo(1);
        assertThat(AttributeLevels.levelOf(25)).isEqualTo(2);
        assertThat(AttributeLevels.levelOf(1124)).isEqualTo(9);
        assertThat(AttributeLevels.levelOf(1125)).isEqualTo(10);
    }
}
