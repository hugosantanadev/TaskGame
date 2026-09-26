package com.gasmtask.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class UserTest {

    private static final Instant CREATED = Instant.parse("2026-09-01T10:00:00Z");

    @Test
    void registerNormalizesEmailAndNameAndStartsVisibleInRanking() {
        User user = User.register("  Hugo@GasmTask.App ", "{bcrypt}hash", "  Hugo  ",
                ZoneId.of("America/Recife"), CREATED);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("hugo@gasmtask.app");
        assertThat(user.getDisplayName()).isEqualTo("Hugo");
        assertThat(user.zoneId()).isEqualTo(ZoneId.of("America/Recife"));
        assertThat(user.isRankingVisible()).isTrue();
        assertThat(user.getCreatedAt()).isEqualTo(CREATED);
        assertThat(user.getUpdatedAt()).isEqualTo(CREATED);
    }

    @Test
    void profileChangesMoveTheUpdateTimestamp() {
        User user = User.register("hugo@gasmtask.app", "{bcrypt}hash", "Hugo", ZoneId.of("UTC"), CREATED);
        Instant later = CREATED.plusSeconds(3600);

        user.rename(" Hugo M. ", later);
        user.changeTimeZone(ZoneId.of("America/Sao_Paulo"), later);
        user.changeRankingVisibility(false, later);

        assertThat(user.getDisplayName()).isEqualTo("Hugo M.");
        assertThat(user.getTimeZone()).isEqualTo("America/Sao_Paulo");
        assertThat(user.isRankingVisible()).isFalse();
        assertThat(user.getUpdatedAt()).isEqualTo(later);
        assertThat(user.getCreatedAt()).isEqualTo(CREATED);
    }
}
