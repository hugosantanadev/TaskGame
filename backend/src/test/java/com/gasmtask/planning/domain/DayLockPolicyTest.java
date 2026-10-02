package com.gasmtask.planning.domain;

import static com.gasmtask.task.domain.TaskKind.EXTRA;
import static com.gasmtask.task.domain.TaskKind.MANDATORY;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class DayLockPolicyTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 30);
    private static final LocalDate YESTERDAY = TODAY.minusDays(1);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);

    @Test
    void passadoNaoAceitaNada() {
        assertThat(DayLockPolicy.canPlace(MANDATORY, YESTERDAY, TODAY, true)).isFalse();
        assertThat(DayLockPolicy.canPlace(EXTRA, YESTERDAY, TODAY, true)).isFalse();
        assertThat(DayLockPolicy.canTakeOut(YESTERDAY, TODAY, true)).isFalse();
    }

    @Test
    void hojeAceitaIncluirObrigatoriaMasNaoTirar() {
        assertThat(DayLockPolicy.canPlace(MANDATORY, TODAY, TODAY, false)).isTrue();
        assertThat(DayLockPolicy.canTakeOut(TODAY, TODAY, false)).isFalse();
    }

    @Test
    void extrasSoAPartirDeAmanha() {
        assertThat(DayLockPolicy.canPlace(EXTRA, TODAY, TODAY, false)).isFalse();
        assertThat(DayLockPolicy.canPlace(EXTRA, TOMORROW, TODAY, false)).isTrue();
    }

    @Test
    void primeiroDiaLiberaTudoEmHoje() {
        assertThat(DayLockPolicy.canPlace(EXTRA, TODAY, TODAY, true)).isTrue();
        assertThat(DayLockPolicy.canTakeOut(TODAY, TODAY, true)).isTrue();
    }

    @Test
    void futuroAceitaTudo() {
        assertThat(DayLockPolicy.canPlace(MANDATORY, TOMORROW, TODAY, false)).isTrue();
        assertThat(DayLockPolicy.canTakeOut(TOMORROW, TODAY, false)).isTrue();
    }
}
