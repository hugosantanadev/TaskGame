package com.gasmtask.shared.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class TimeOfDayTest {

    @Test
    void limitesDosPeriodos() {
        assertThat(TimeOfDay.of(LocalTime.of(4, 59))).isEqualTo(TimeOfDay.NIGHT);
        assertThat(TimeOfDay.of(LocalTime.of(5, 0))).isEqualTo(TimeOfDay.MORNING);
        assertThat(TimeOfDay.of(LocalTime.of(11, 59))).isEqualTo(TimeOfDay.MORNING);
        assertThat(TimeOfDay.of(LocalTime.of(12, 0))).isEqualTo(TimeOfDay.AFTERNOON);
        assertThat(TimeOfDay.of(LocalTime.of(16, 59))).isEqualTo(TimeOfDay.AFTERNOON);
        assertThat(TimeOfDay.of(LocalTime.of(17, 0))).isEqualTo(TimeOfDay.SUNSET);
        assertThat(TimeOfDay.of(LocalTime.of(18, 59))).isEqualTo(TimeOfDay.SUNSET);
        assertThat(TimeOfDay.of(LocalTime.of(19, 0))).isEqualTo(TimeOfDay.NIGHT);
        assertThat(TimeOfDay.of(LocalTime.MIDNIGHT)).isEqualTo(TimeOfDay.NIGHT);
    }
}
