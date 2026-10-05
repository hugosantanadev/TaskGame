package com.gasmtask.economy.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.gasmtask.task.domain.TaskKind;

import org.junit.jupiter.api.Test;

class RewardPolicyTest {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate DAY = LocalDate.of(2026, 9, 28);
    private static final LocalTime EIGHT = LocalTime.of(8, 0);

    private final RewardPolicy policy = new RewardPolicy(5, 3, 1, 2, 1, 1, 1, Duration.ofMinutes(30));

    private static ZonedDateTime at(int hour, int minute) {
        return ZonedDateTime.of(DAY, LocalTime.of(hour, minute), SAO_PAULO);
    }

    @Test
    void obrigatoriaNoHorarioEComProvaRendeCincoMoedas() {
        Reward reward = policy.rewardFor(5, policy.baseCoinsFor(TaskKind.MANDATORY), true, true, 0);

        assertThat(reward).isEqualTo(new Reward(5, 3, 1, 1, 0));
        assertThat(reward.totalCoins()).isEqualTo(5);
    }

    @Test
    void melhoriaDoQuartoSomaAsMoedasDaCategoria() {
        Reward reward = policy.rewardFor(5, policy.baseCoinsFor(TaskKind.MANDATORY), false, false, 2);

        assertThat(reward.equipmentBonus()).isEqualTo(2);
        assertThat(reward.totalCoins()).isEqualTo(5);
    }

    @Test
    void extraRendeUmaMoedaEPontosDentroDaFaixa() {
        assertThat(policy.baseCoinsFor(TaskKind.EXTRA)).isEqualTo(1);
        assertThat(policy.acceptsExtraPoints(2)).isTrue();
        assertThat(policy.acceptsExtraPoints(3)).isFalse();
    }

    @Test
    void janelaDoHorarioVaiDeTrintaMinutosAntesATrintaDepoisDoFim() {
        assertThat(policy.isOnTime(DAY, EIGHT, 60, true, at(7, 29))).isFalse();
        assertThat(policy.isOnTime(DAY, EIGHT, 60, true, at(7, 30))).isTrue();
        assertThat(policy.isOnTime(DAY, EIGHT, 60, true, at(9, 30))).isTrue();
        assertThat(policy.isOnTime(DAY, EIGHT, 60, true, at(9, 31))).isFalse();
    }

    @Test
    void semDuracaoAJanelaFechaTrintaMinutosDepoisDoHorario() {
        assertThat(policy.isOnTime(DAY, EIGHT, null, true, at(8, 30))).isTrue();
        assertThat(policy.isOnTime(DAY, EIGHT, null, true, at(8, 31))).isFalse();
    }

    @Test
    void semHorarioOuPlanejadaNoProprioDiaNaoHaBonus() {
        assertThat(policy.isOnTime(DAY, null, 60, true, at(8, 0))).isFalse();
        assertThat(policy.isOnTime(DAY, EIGHT, 60, false, at(8, 0))).isFalse();
    }

    @Test
    void recusaConfiguracaoIncoerente() {
        assertThatThrownBy(() -> new RewardPolicy(5, 3, 3, 2, 1, 1, 1, Duration.ofMinutes(30)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RewardPolicy(5, 3, 1, 2, 1, 1, 1, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
