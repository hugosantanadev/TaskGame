package com.gasmtask.task.domain;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;

import org.junit.jupiter.api.Test;

class ScheduleDistributorTest {

    @Test
    void sugereDiasEspacadosPriorizandoSegundaASexta() {
        assertThat(ScheduleDistributor.suggest(1)).containsExactly(WEDNESDAY);
        assertThat(ScheduleDistributor.suggest(2)).containsExactly(TUESDAY, THURSDAY);
        assertThat(ScheduleDistributor.suggest(3)).containsExactly(MONDAY, WEDNESDAY, FRIDAY);
        assertThat(ScheduleDistributor.suggest(4)).containsExactly(MONDAY, TUESDAY, THURSDAY, FRIDAY);
        assertThat(ScheduleDistributor.suggest(5)).containsExactly(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        assertThat(ScheduleDistributor.suggest(6)).containsExactly(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY);
        assertThat(ScheduleDistributor.suggest(7)).containsExactly(DayOfWeek.values());
    }

    @Test
    void recusaQuantidadeForaDeUmASete() {
        assertThatThrownBy(() -> ScheduleDistributor.suggest(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ScheduleDistributor.suggest(8)).isInstanceOf(IllegalArgumentException.class);
    }
}
