package com.gasmtask.streak.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;

import com.gasmtask.planning.domain.DayProgress;

import org.junit.jupiter.api.Test;

class StreakTest {

    private static final LocalDate START = LocalDate.of(2026, 9, 28);
    private static final DayProgress FULFILLED = new DayProgress(2, 2, 0, 0, 10, 6);
    private static final DayProgress PENDING = new DayProgress(2, 1, 0, 0, 5, 3);
    private static final DayProgress REST = new DayProgress(0, 0, 1, 1, 2, 1);

    @Test
    void statusDoDiaSegueAsObrigatorias() {
        assertThat(StreakRules.statusOf(0, 0)).isEqualTo(DayStatus.REST);
        assertThat(StreakRules.statusOf(2, 2)).isEqualTo(DayStatus.FULFILLED);
        assertThat(StreakRules.statusOf(2, 1)).isEqualTo(DayStatus.FAILED);
    }

    @Test
    void cumpridoSomaDescansoMantemFalhaZera() {
        Streak streak = Streak.start(UUID.randomUUID(), START);

        streak.close(START, DayStatus.FULFILLED);
        streak.close(START.plusDays(1), DayStatus.FULFILLED);
        streak.close(START.plusDays(2), DayStatus.REST);
        assertThat(streak.getCurrentStreak()).isEqualTo(2);

        streak.close(START.plusDays(3), DayStatus.FAILED);
        streak.close(START.plusDays(4), DayStatus.FULFILLED);

        assertThat(streak.getCurrentStreak()).isEqualTo(1);
        assertThat(streak.getLongestStreak()).isEqualTo(2);
        assertThat(streak.getLastFulfilledDate()).isEqualTo(START.plusDays(4));
        assertThat(streak.firstOpenDate()).isEqualTo(START.plusDays(5));
    }

    @Test
    void diasFechamEmOrdem() {
        Streak streak = Streak.start(UUID.randomUUID(), START);

        assertThatThrownBy(() -> streak.close(START.plusDays(1), DayStatus.FULFILLED))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void hojeCumpridoSobeNaHoraEVoltaSeGanharPendencia() {
        Streak streak = Streak.start(UUID.randomUUID(), START);
        streak.close(START, DayStatus.FULFILLED);
        LocalDate today = START.plusDays(1);

        StreakView done = streak.view(today, FULFILLED);
        StreakView reopened = streak.view(today, PENDING);

        assertThat(done.current()).isEqualTo(2);
        assertThat(done.longest()).isEqualTo(2);
        assertThat(done.todayStatus()).isEqualTo(TodayStatus.FULFILLED);
        assertThat(done.lastFulfilledDate()).isEqualTo(today);
        assertThat(reopened.current()).isEqualTo(1);
        assertThat(reopened.todayStatus()).isEqualTo(TodayStatus.PENDING);
    }

    @Test
    void diaSemObrigatoriaEDescanso() {
        Streak streak = Streak.start(UUID.randomUUID(), START);

        assertThat(streak.view(START, REST).todayStatus()).isEqualTo(TodayStatus.REST);
        assertThat(streak.view(START, REST).current()).isZero();
    }

    @Test
    void protetorTransformaFalhaEmDiaProtegidoEMantemASequencia() {
        Streak streak = Streak.start(UUID.randomUUID(), START);
        streak.close(START, DayStatus.FULFILLED);
        streak.addFreeze(2);

        DayStatus saved = streak.close(START.plusDays(1), DayStatus.FAILED);
        assertThat(saved).isEqualTo(DayStatus.FROZEN);
        assertThat(streak.getCurrentStreak()).isEqualTo(1);
        assertThat(streak.getFreezes()).isZero();
        assertThat(streak.getLastFrozenDate()).isEqualTo(START.plusDays(1));

        // Sem protetor, a próxima falha zera
        assertThat(streak.close(START.plusDays(2), DayStatus.FAILED)).isEqualTo(DayStatus.FAILED);
        assertThat(streak.getCurrentStreak()).isZero();
    }

    @Test
    void protetoresTemLimite() {
        Streak streak = Streak.start(UUID.randomUUID(), START);
        streak.addFreeze(2);
        streak.addFreeze(2);

        assertThatThrownBy(() -> streak.addFreeze(2)).isInstanceOf(IllegalStateException.class);
    }
}
