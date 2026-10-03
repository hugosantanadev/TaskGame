package com.gasmtask.stats.service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.gasmtask.achievement.service.AchievementService;
import com.gasmtask.economy.dto.WalletResponse;
import com.gasmtask.economy.service.WalletService;
import com.gasmtask.planning.domain.OccurrenceStatus;
import com.gasmtask.planning.repository.DailyCount;
import com.gasmtask.planning.repository.TaskOccurrenceRepository;
import com.gasmtask.shared.exception.BusinessException;
import com.gasmtask.shared.exception.ErrorCode;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.stats.domain.PeriodTotals;
import com.gasmtask.stats.domain.StatsGranularity;
import com.gasmtask.stats.domain.SummaryDayStatus;
import com.gasmtask.stats.dto.StatsHistoryResponse;
import com.gasmtask.stats.dto.StatsOverviewResponse;
import com.gasmtask.stats.dto.TotalsResponse;
import com.gasmtask.stats.dto.WeekSummaryResponse;
import com.gasmtask.streak.domain.DayStatus;
import com.gasmtask.streak.dto.StreakResponse;
import com.gasmtask.streak.service.DayClosingService;
import com.gasmtask.streak.service.StreakService;
import com.gasmtask.user.service.UserService;
import com.gasmtask.user.service.UserTimeInfo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Estatísticas e resumo semanal (RF21, RF22). Só leitura: tudo sai das ocorrências, dos dias fechados e da
 * carteira. Fecha os dias pendentes antes, para que "perdidas" e o streak já estejam em dia.
 */
@Service
public class StatsService {

    /** Meio ano de semanas ou dois anos de meses: o bastante para o gráfico e barato de calcular. */
    static final int MAX_PERIODS = 26;

    private final TaskOccurrenceRepository occurrences;
    private final DayClosingService closing;
    private final StreakService streaks;
    private final WalletService wallet;
    private final AchievementService achievements;
    private final UserService users;
    private final UserCalendar calendar;

    public StatsService(TaskOccurrenceRepository occurrences, DayClosingService closing, StreakService streaks,
                        WalletService wallet, AchievementService achievements, UserService users,
                        UserCalendar calendar) {
        this.occurrences = occurrences;
        this.closing = closing;
        this.streaks = streaks;
        this.wallet = wallet;
        this.achievements = achievements;
        this.users = users;
        this.calendar = calendar;
    }

    @Transactional
    public StatsOverviewResponse overview(UUID userId) {
        StreakResponse streak = streaks.current(userId); // fecha os dias pendentes antes de contar
        WalletResponse coins = wallet.summary(userId);
        Map<DayStatus, Integer> days = streaks.closedDays(userId);
        int completed = (int) occurrences.countByUserIdAndStatus(userId, OccurrenceStatus.COMPLETED);
        int missed = (int) occurrences.countByUserIdAndStatus(userId, OccurrenceStatus.MISSED);
        List<StatsOverviewResponse.CategoryTotal> byCategory = occurrences
                .countByCategory(userId, OccurrenceStatus.COMPLETED).stream()
                .map(count -> new StatsOverviewResponse.CategoryTotal(count.getCategory(), (int) count.getTotal()))
                .sorted(Comparator.comparingInt(StatsOverviewResponse.CategoryTotal::completed).reversed()
                        .thenComparing(StatsOverviewResponse.CategoryTotal::category))
                .toList();
        return new StatsOverviewResponse(
                completed,
                missed,
                completed + missed == 0 ? null : completed * 100 / (completed + missed),
                (int) occurrences.sumEarnedPoints(userId),
                coins.totalEarned(),
                coins.totalSpent(),
                coins.balance(),
                streak.current(),
                streak.longest(),
                days.get(DayStatus.FULFILLED),
                days.get(DayStatus.FAILED),
                days.get(DayStatus.REST),
                days.get(DayStatus.FROZEN),
                achievements.unlockedCount(userId),
                byCategory);
    }

    @Transactional
    public StatsHistoryResponse history(UUID userId, StatsGranularity granularity, int periods) {
        closing.closePendingDays(userId);
        UserTimeInfo info = users.timeInfo(userId);
        LocalDate today = calendar.today(info.zone());
        List<LocalDate> starts = granularity.lastPeriods(today, info.registrationDate(),
                Math.clamp(periods, 1, MAX_PERIODS));
        Map<LocalDate, PeriodTotals> byPeriod = totalsBy(userId, starts.getFirst(),
                granularity.endOf(starts.getLast()), granularity::startOf);
        LocalDate currentStart = granularity.startOf(today);
        return new StatsHistoryResponse(granularity, starts.stream()
                .map(start -> new StatsHistoryResponse.Period(start, granularity.endOf(start),
                        start.equals(currentStart),
                        TotalsResponse.of(byPeriod.getOrDefault(start, PeriodTotals.EMPTY))))
                .toList());
    }

    @Transactional
    public WeekSummaryResponse weekSummary(UUID userId, LocalDate weekStart) {
        if (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new BusinessException(ErrorCode.INVALID_WEEK_START);
        }
        closing.closePendingDays(userId);
        ZoneId zone = users.timeInfo(userId).zone();
        LocalDate today = calendar.today(zone);
        LocalDate weekEnd = weekStart.plusDays(6);
        Map<LocalDate, PeriodTotals> byDate = totalsBy(userId, weekStart, weekEnd, Function.identity());
        Map<LocalDate, DayStatus> closed = streaks.closedStatuses(userId, weekStart, weekEnd);

        List<WeekSummaryResponse.Day> days = new ArrayList<>();
        PeriodTotals week = PeriodTotals.EMPTY;
        int fulfilledDays = 0;
        for (LocalDate date = weekStart; !date.isAfter(weekEnd); date = date.plusDays(1)) {
            PeriodTotals day = byDate.getOrDefault(date, PeriodTotals.EMPTY);
            SummaryDayStatus status = closed.get(date) == DayStatus.FROZEN
                    ? SummaryDayStatus.FROZEN
                    : SummaryDayStatus.of(date, today, day.mandatoryPlanned(), day.mandatoryDone());
            days.add(new WeekSummaryResponse.Day(date, date.getDayOfWeek(), status, TotalsResponse.of(day)));
            week = week.plus(day);
            fulfilledDays += status == SummaryDayStatus.FULFILLED ? 1 : 0;
        }

        Instant from = weekStart.atStartOfDay(zone).toInstant();
        Instant to = weekEnd.plusDays(1).atStartOfDay(zone).toInstant();
        return new WeekSummaryResponse(weekStart, weekEnd, today.isAfter(weekEnd), TotalsResponse.of(week),
                fulfilledDays, days, achievements.unlockedBetween(userId, from, to));
    }

    /** Soma as contagens diárias de [from, to], agrupando cada dia pela chave (o próprio dia ou o início do período). */
    private Map<LocalDate, PeriodTotals> totalsBy(UUID userId, LocalDate from, LocalDate to,
                                                  Function<LocalDate, LocalDate> key) {
        Map<LocalDate, PeriodTotals> totals = new HashMap<>();
        for (DailyCount count : occurrences.countByDay(userId, from, to)) {
            PeriodTotals row = PeriodTotals.EMPTY.plus(count.getKind(), count.getStatus(), (int) count.getTotal(),
                    (int) count.getPoints(), (int) count.getCoins());
            totals.merge(key.apply(count.getDate()), row, PeriodTotals::plus);
        }
        return totals;
    }
}
