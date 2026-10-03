package com.gasmtask.progression.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class RankLadderTest {

    private static final Instant NOW = Instant.parse("2026-10-02T12:00:00Z");

    @Test
    void escadaVaiDoFerro1ALendaSempreSubindo() {
        assertThat(RankLadder.STEPS).hasSize(22);
        assertThat(RankLadder.STEPS.getFirst()).isEqualTo(new Rank(RankTier.IRON, 1, 0));
        assertThat(RankLadder.STEPS.getLast()).isEqualTo(new Rank(RankTier.LEGEND, null, 7000));
        for (int i = 1; i < RankLadder.STEPS.size(); i++) {
            assertThat(RankLadder.STEPS.get(i).isAbove(RankLadder.STEPS.get(i - 1))).isTrue();
        }
        // Todo elo abaixo de Lenda tem as três divisões
        assertThat(RankLadder.STEPS.stream().filter(step -> step.tier() == RankTier.GOLD))
                .extracting(Rank::division).containsExactly(1, 2, 3);
    }

    @Test
    void eloEODegrauMaisAltoQueOXpAlcancou() {
        assertThat(RankLadder.rankOf(0)).isEqualTo(new Rank(RankTier.IRON, 1, 0));
        assertThat(RankLadder.rankOf(49).division()).isEqualTo(1);
        assertThat(RankLadder.rankOf(50).division()).isEqualTo(2);
        assertThat(RankLadder.rankOf(174).tier()).isEqualTo(RankTier.IRON);
        assertThat(RankLadder.rankOf(175)).isEqualTo(new Rank(RankTier.BRONZE, 1, 175));
        assertThat(RankLadder.rankOf(99_999).tier()).isEqualTo(RankTier.LEGEND);
    }

    @Test
    void proximoDegrauSomeNoTopo() {
        assertThat(RankLadder.nextAfter(RankLadder.rankOf(0))).contains(new Rank(RankTier.IRON, 2, 50));
        assertThat(RankLadder.nextAfter(RankLadder.rankOf(7000))).isEmpty();
    }

    @Test
    void elosAlcancadosDizemQuaisRoupasJaForamGanhas() {
        assertThat(RankLadder.tiersReachedBy(100)).containsExactly(RankTier.IRON);
        assertThat(RankLadder.tiersReachedBy(960))
                .containsExactly(RankTier.IRON, RankTier.BRONZE, RankTier.SILVER, RankTier.GOLD);
        assertThat(RankTier.IRON.rewardCode()).isNull();
        assertThat(RankTier.LEGEND.hasDivisions()).isFalse();
    }

    @Test
    void xpNuncaFicaNegativoEOPicoNaoCai() {
        PlayerProgress progress = PlayerProgress.start(UUID.randomUUID(), NOW);

        assertThat(progress.apply(200, NOW)).isEqualTo(200);
        assertThat(progress.rank().tier()).isEqualTo(RankTier.BRONZE);

        assertThat(progress.apply(-30, NOW)).isEqualTo(-30);
        assertThat(progress.rank().tier()).isEqualTo(RankTier.IRON);   // caiu de elo...
        assertThat(progress.peakRank().tier()).isEqualTo(RankTier.BRONZE); // ...mas o pico fica

        // Uma perda maior que o saldo só leva até zero
        assertThat(progress.apply(-500, NOW)).isEqualTo(-170);
        assertThat(progress.getXp()).isZero();
        assertThat(progress.getPeakXp()).isEqualTo(200);
    }
}
