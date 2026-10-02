package com.gasmtask.stats.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import com.gasmtask.planning.domain.OccurrenceStatus;
import com.gasmtask.task.domain.TaskKind;

import org.junit.jupiter.api.Test;

class StatsRulesTest {

    /** Uma quarta-feira. */
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 30);

    @Test
    void totaisSeparamObrigatoriasExtrasEPerdidas() {
        PeriodTotals totals = PeriodTotals.EMPTY
                .plus(TaskKind.MANDATORY, OccurrenceStatus.COMPLETED, 3, 15, 12)
                .plus(TaskKind.MANDATORY, OccurrenceStatus.MISSED, 1, 0, 0)
                .plus(TaskKind.EXTRA, OccurrenceStatus.COMPLETED, 1, 2, 1)
                .plus(TaskKind.EXTRA, OccurrenceStatus.PENDING, 1, 0, 0);

        assertThat(totals).isEqualTo(new PeriodTotals(4, 3, 2, 1, 1, 17, 13));
        assertThat(totals.planned()).isEqualTo(6);
        assertThat(totals.done()).isEqualTo(4);
        assertThat(totals.plus(totals)).isEqualTo(new PeriodTotals(8, 6, 4, 2, 2, 34, 26));
    }

    @Test
    void taxaArredondaParaBaixoESomeSemNadaPlanejado() {
        assertThat(PeriodTotals.EMPTY.completionRate()).isNull();
        assertThat(new PeriodTotals(3, 2, 0, 0, 1, 10, 6).completionRate()).isEqualTo(66);
        // 199 de 200 não pode aparecer como 100%
        assertThat(new PeriodTotals(200, 199, 0, 0, 1, 0, 0).completionRate()).isEqualTo(99);
        assertThat(new PeriodTotals(2, 2, 1, 1, 0, 12, 7).completionRate()).isEqualTo(100);
    }

    @Test
    void semanasComecamNaSegundaEMesesNoDia1() {
        assertThat(StatsGranularity.WEEK.startOf(TODAY)).isEqualTo(LocalDate.of(2026, 9, 28));
        assertThat(StatsGranularity.WEEK.endOf(LocalDate.of(2026, 9, 28))).isEqualTo(LocalDate.of(2026, 10, 4));
        assertThat(StatsGranularity.MONTH.startOf(TODAY)).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(StatsGranularity.MONTH.endOf(LocalDate.of(2026, 2, 1))).isEqualTo(LocalDate.of(2026, 2, 28));
    }

    @Test
    void ultimosPeriodosVaoDoMaisAntigoAoAtualSemPassarDoCadastro() {
        assertThat(StatsGranularity.WEEK.lastPeriods(TODAY, LocalDate.of(2026, 1, 5), 3)).containsExactly(
                LocalDate.of(2026, 9, 14), LocalDate.of(2026, 9, 21), LocalDate.of(2026, 9, 28));
        // Cadastro na semana passada: só duas semanas existem, mesmo pedindo oito
        assertThat(StatsGranularity.WEEK.lastPeriods(TODAY, LocalDate.of(2026, 9, 24), 8)).containsExactly(
                LocalDate.of(2026, 9, 21), LocalDate.of(2026, 9, 28));
        assertThat(StatsGranularity.WEEK.lastPeriods(TODAY, TODAY, 8)).containsExactly(LocalDate.of(2026, 9, 28));
        assertThat(StatsGranularity.MONTH.lastPeriods(TODAY, LocalDate.of(2026, 7, 31), 12)).containsExactly(
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 1));
    }

    @Test
    void diaDoResumoSegueOFechamentoEHojeAindaPodeVirar() {
        LocalDate yesterday = TODAY.minusDays(1);
        assertThat(SummaryDayStatus.of(yesterday, TODAY, 2, 2)).isEqualTo(SummaryDayStatus.FULFILLED);
        assertThat(SummaryDayStatus.of(yesterday, TODAY, 2, 1)).isEqualTo(SummaryDayStatus.FAILED);
        assertThat(SummaryDayStatus.of(yesterday, TODAY, 0, 0)).isEqualTo(SummaryDayStatus.REST);

        assertThat(SummaryDayStatus.of(TODAY, TODAY, 2, 1)).isEqualTo(SummaryDayStatus.PENDING);
        assertThat(SummaryDayStatus.of(TODAY, TODAY, 2, 2)).isEqualTo(SummaryDayStatus.FULFILLED);
        assertThat(SummaryDayStatus.of(TODAY, TODAY, 0, 0)).isEqualTo(SummaryDayStatus.REST);

        assertThat(SummaryDayStatus.of(TODAY.plusDays(1), TODAY, 2, 0)).isEqualTo(SummaryDayStatus.UPCOMING);
    }
}
