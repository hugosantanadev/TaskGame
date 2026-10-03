package com.gasmtask.streak.domain;

import java.time.LocalDate;
import java.util.UUID;

import com.gasmtask.planning.domain.DayProgress;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Streak do usuário. Grava só os dias já fechados; o dia de hoje entra na leitura ({@link #view}).
 * Assim o streak sobe na hora em que a última obrigatória é concluída (RN23) e, se o usuário incluir
 * outra obrigatória em hoje depois disso, volta a ficar pendente sem precisar desfazer nada no banco.
 */
@Entity
@Table(name = "streaks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Streak {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak;

    @Column(name = "longest_streak", nullable = false)
    private int longestStreak;

    @Column(name = "last_fulfilled_date")
    private LocalDate lastFulfilledDate;

    @Column(name = "last_closed_date")
    private LocalDate lastClosedDate;

    @Column(name = "tracking_start_date", nullable = false, updatable = false)
    private LocalDate trackingStartDate;

    /** Protetores de sequência guardados. */
    @Column(nullable = false)
    private int freezes;

    /** Último dia salvo por um protetor. */
    @Column(name = "last_frozen_date")
    private LocalDate lastFrozenDate;

    @Version
    private Long version;

    public static Streak start(UUID userId, LocalDate trackingStartDate) {
        Streak streak = new Streak();
        streak.userId = userId;
        streak.trackingStartDate = trackingStartDate;
        return streak;
    }

    /** Primeiro dia ainda não fechado. */
    public LocalDate firstOpenDate() {
        return lastClosedDate == null ? trackingStartDate : lastClosedDate.plusDays(1);
    }

    /**
     * Fecha um dia, em ordem, um de cada vez, e devolve como ele fechou de fato: uma falha com protetor
     * guardado consome o protetor e vira dia protegido, que mantém a sequência.
     */
    public DayStatus close(LocalDate date, DayStatus status) {
        if (!date.equals(firstOpenDate())) {
            throw new IllegalStateException("Os dias fecham em ordem: esperado " + firstOpenDate() + ", recebido " + date);
        }
        if (status == DayStatus.FAILED && freezes > 0) {
            freezes--;
            lastFrozenDate = date;
            status = DayStatus.FROZEN;
        }
        currentStreak = StreakRules.next(currentStreak, status);
        if (status == DayStatus.FULFILLED) {
            lastFulfilledDate = date;
        }
        longestStreak = Math.max(longestStreak, currentStreak);
        lastClosedDate = date;
        return status;
    }

    /** Guarda mais um protetor, até o máximo. */
    public void addFreeze(int max) {
        if (freezes >= max) {
            throw new IllegalStateException("Já tem o máximo de protetores: " + max);
        }
        freezes++;
    }

    public StreakView view(LocalDate today, DayProgress todayProgress) {
        boolean todayOpen = lastClosedDate == null || today.isAfter(lastClosedDate);
        boolean fulfilledToday = todayOpen && todayProgress.fulfilled();
        int current = currentStreak + (fulfilledToday ? 1 : 0);
        TodayStatus status = !todayProgress.hasMandatory()
                ? TodayStatus.REST
                : fulfilledToday ? TodayStatus.FULFILLED : TodayStatus.PENDING;
        return new StreakView(current, Math.max(longestStreak, current), status,
                fulfilledToday ? today : lastFulfilledDate, freezes, lastFrozenDate);
    }
}
