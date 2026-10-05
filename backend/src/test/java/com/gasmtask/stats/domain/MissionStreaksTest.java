package com.gasmtask.stats.domain;

import static com.gasmtask.planning.domain.OccurrenceStatus.COMPLETED;
import static com.gasmtask.planning.domain.OccurrenceStatus.MISSED;
import static com.gasmtask.planning.domain.OccurrenceStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class MissionStreaksTest {

    @Test
    void perdidaQuebraEPendenteNaoConta() {
        assertThat(MissionStreaks.of(List.of())).isEqualTo(new MissionStreaks(0, 0));
        assertThat(MissionStreaks.of(List.of(COMPLETED, COMPLETED, MISSED, COMPLETED, PENDING, PENDING)))
                .isEqualTo(new MissionStreaks(1, 2));
        assertThat(MissionStreaks.of(List.of(COMPLETED, COMPLETED, COMPLETED, MISSED)))
                .isEqualTo(new MissionStreaks(0, 3));
    }
}
