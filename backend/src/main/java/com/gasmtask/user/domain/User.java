package com.gasmtask.user.domain;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Identidade e perfil do usuário. O fuso horário é parte do domínio: "hoje", "amanhã" e "semana"
 * são sempre calculados nele, nunca no fuso do servidor.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    private UUID id;

    @Column(nullable = false, length = 254, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 40)
    private String displayName;

    @Column(name = "time_zone", nullable = false, length = 64)
    private String timeZone;

    @Column(name = "ranking_visible", nullable = false)
    private boolean rankingVisible;

    @Embedded
    private ReminderSettings reminderSettings;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public static User register(String email, String passwordHash, String displayName, ZoneId zone, Instant now) {
        User user = new User();
        user.id = UUID.randomUUID();
        user.email = normalizeEmail(email);
        user.passwordHash = passwordHash;
        user.displayName = displayName.strip();
        user.timeZone = zone.getId();
        user.rankingVisible = true;
        user.reminderSettings = ReminderSettings.defaults();
        user.createdAt = now;
        user.updatedAt = now;
        return user;
    }

    public void rename(String newDisplayName, Instant now) {
        this.displayName = newDisplayName.strip();
        this.updatedAt = now;
    }

    public void changeTimeZone(ZoneId zone, Instant now) {
        this.timeZone = zone.getId();
        this.updatedAt = now;
    }

    public void changeRankingVisibility(boolean visible, Instant now) {
        this.rankingVisible = visible;
        this.updatedAt = now;
    }

    public void changeReminderSettings(ReminderSettings settings, Instant now) {
        this.reminderSettings = settings;
        this.updatedAt = now;
    }

    public ZoneId zoneId() {
        return ZoneId.of(timeZone);
    }

    public static String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
