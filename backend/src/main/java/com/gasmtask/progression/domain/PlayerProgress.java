package com.gasmtask.progression.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * XP atual (define o elo, sobe e desce) e o maior XP já alcançado (guarda as roupas desbloqueadas:
 * cair de elo não tira o que já foi ganho). O XP nunca fica negativo, como o saldo de moedas.
 */
@Entity
@Table(name = "player_progress")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerProgress {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private int xp;

    @Column(name = "peak_xp", nullable = false)
    private int peakXp;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public static PlayerProgress start(UUID userId, Instant now) {
        PlayerProgress progress = new PlayerProgress();
        progress.userId = userId;
        progress.updatedAt = now;
        return progress;
    }

    /**
     * Soma {@code delta} ao XP sem deixar ficar negativo e devolve o quanto de fato mudou
     * (uma perda maior que o saldo leva só até zero).
     */
    public int apply(int delta, Instant now) {
        int before = xp;
        xp = Math.max(0, xp + delta);
        peakXp = Math.max(peakXp, xp);
        updatedAt = now;
        return xp - before;
    }

    public Rank rank() {
        return RankLadder.rankOf(xp);
    }

    public Rank peakRank() {
        return RankLadder.rankOf(peakXp);
    }
}
