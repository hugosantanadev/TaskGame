package com.gasmtask.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalTime;
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

    @Test
    void reminderSettingsStartWithDefaultsAndRejectLeadOutOfRange() {
        User user = User.register("hugo@gasmtask.app", "{bcrypt}hash", "Hugo", ZoneId.of("UTC"), CREATED);

        assertThat(user.getReminderSettings().isTasksEnabled()).isTrue();
        assertThat(user.getReminderSettings().getLeadMinutes()).isEqualTo(10);
        assertThat(user.getReminderSettings().getBedtime()).isNull();

        Instant later = CREATED.plusSeconds(60);
        user.changeReminderSettings(ReminderSettings.of(false, 30, LocalTime.of(23, 0), LocalTime.of(6, 30)), later);
        assertThat(user.getReminderSettings().getWakeTime()).isEqualTo(LocalTime.of(6, 30));
        assertThat(user.getUpdatedAt()).isEqualTo(later);

        assertThatThrownBy(() -> ReminderSettings.of(true, ReminderSettings.MAX_LEAD_MINUTES + 1, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
