package com.gasmtask.stats.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gasmtask.planning.domain.OccurrenceStatus;
import com.gasmtask.planning.domain.TaskOccurrence;
import com.gasmtask.planning.repository.TaskOccurrenceRepository;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.stats.domain.MissionStreaks;
import com.gasmtask.stats.domain.PeriodTotals;
import com.gasmtask.stats.domain.StatsGranularity;
import com.gasmtask.stats.dto.MissionEvolutionResponse;
import com.gasmtask.stats.dto.MissionSummaryResponse;
import com.gasmtask.stats.dto.StatsHistoryResponse;
import com.gasmtask.stats.dto.TotalsResponse;
import com.gasmtask.streak.service.DayClosingService;
import com.gasmtask.task.dto.TaskResponse;
import com.gasmtask.task.service.TaskService;
import com.gasmtask.user.service.UserService;
import com.gasmtask.user.service.UserTimeInfo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Evolução por missão: quanto cada missão (academia, estudos...) foi feita, semana a semana, e a sequência dela.
 * Só leitura, a partir das ocorrências; fecha os dias pendentes antes para que as perdidas já contem.
 */
@Service
public class MissionStatsService {

    private final TaskOccurrenceRepository occurrences;
    private final TaskService tasks;
    private final DayClosingService closing;
    private final UserService users;
    private final UserCalendar calendar;

    public MissionStatsService(TaskOccurrenceRepository occurrences, TaskService tasks, DayClosingService closing,
                               UserService users, UserCalendar calendar) {
        this.occurrences = occurrences;
        this.tasks = tasks;
        this.closing = closing;
        this.users = users;
        this.calendar = calendar;
    }

    /** Todas as missões (ativas e arquivadas), das mais feitas para as menos feitas. */
    @Transactional
    public List<MissionSummaryResponse> missions(UUID userId) {
        closing.closePendingDays(userId);
        Map<UUID, List<TaskOccurrence>> byTask = occurrences
                .findByUserIdAndTaskIdIsNotNullOrderByOccurrenceDateAsc(userId).stream()
                .collect(Collectors.groupingBy(TaskOccurrence::getTaskId, LinkedHashMap::new, Collectors.toList()));
        List<TaskResponse> all = new ArrayList<>(tasks.list(userId, false));
        all.addAll(tasks.list(userId, true));
        return all.stream()
                .map(task -> summaryOf(task, byTask.getOrDefault(task.id(), List.of())))
                .sorted(Comparator.comparingInt(MissionSummaryResponse::completed).reversed()
                        .thenComparing(MissionSummaryResponse::name))
                .toList();
    }

    @Transactional
    public MissionEvolutionResponse evolution(UUID userId, UUID taskId, int weeks) {
        closing.closePendingDays(userId);
        TaskResponse task = tasks.get(userId, taskId); // de outra pessoa: 404
        List<TaskOccurrence> list = occurrences.findByUserIdAndTaskIdOrderByOccurrenceDateAsc(userId, taskId);
        UserTimeInfo info = users.timeInfo(userId);
        LocalDate today = calendar.today(info.zone());
        List<LocalDate> starts = StatsGranularity.WEEK.lastPeriods(today, info.registrationDate(),
                Math.clamp(weeks, 1, StatsService.MAX_PERIODS));
        LocalDate from = starts.getFirst();
        LocalDate currentWeek = StatsGranularity.WEEK.startOf(today);

        Map<LocalDate, PeriodTotals> byWeek = new HashMap<>();
        Map<LocalDate, Integer> byMonth = new HashMap<>();
        for (TaskOccurrence occurrence : list) {
            if (occurrence.isCompleted()) {
                byMonth.merge(occurrence.getOccurrenceDate().withDayOfMonth(1), 1, Integer::sum);
            }
            if (occurrence.getOccurrenceDate().isBefore(from)) {
                continue;
            }
            PeriodTotals row = PeriodTotals.EMPTY.plus(occurrence.getKind(), occurrence.getStatus(), 1,
                    nullToZero(occurrence.getEarnedPoints()), nullToZero(occurrence.getEarnedCoins()));
            byWeek.merge(StatsGranularity.WEEK.startOf(occurrence.getOccurrenceDate()), row, PeriodTotals::plus);
        }
        List<StatsHistoryResponse.Period> periods = starts.stream()
                .map(start -> new StatsHistoryResponse.Period(start, StatsGranularity.WEEK.endOf(start),
                        start.equals(currentWeek), TotalsResponse.of(byWeek.getOrDefault(start, PeriodTotals.EMPTY))))
                .toList();
        int doneInRange = periods.stream().mapToInt(period -> period.totals().done()).sum();
        Map.Entry<LocalDate, Integer> bestMonth = byMonth.entrySet().stream()
                .max(Map.Entry.<LocalDate, Integer>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                .orElse(null);

        return new MissionEvolutionResponse(
                summaryOf(task, list),
                periods,
                bestMonth == null ? null : bestMonth.getKey(),
                bestMonth == null ? 0 : bestMonth.getValue(),
                Math.round(doneInRange * 10.0 / periods.size()) / 10.0,
                list.stream().filter(TaskOccurrence::isCompleted).map(TaskOccurrence::getOccurrenceDate)
                        .findFirst().orElse(null));
    }

    private static MissionSummaryResponse summaryOf(TaskResponse task, List<TaskOccurrence> list) {
        int completed = (int) list.stream().filter(TaskOccurrence::isCompleted).count();
        int missed = (int) list.stream().filter(occurrence -> occurrence.getStatus() == OccurrenceStatus.MISSED).count();
        MissionStreaks streaks = MissionStreaks.of(list.stream().map(TaskOccurrence::getStatus).toList());
        LocalDate lastCompleted = list.stream().filter(TaskOccurrence::isCompleted)
                .map(TaskOccurrence::getOccurrenceDate).reduce((first, second) -> second).orElse(null);
        return new MissionSummaryResponse(task.id(), task.name(), task.category(), task.archived(), completed, missed,
                completed + missed == 0 ? null : completed * 100 / (completed + missed), streaks.current(),
                streaks.best(), lastCompleted);
    }

    private static int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }
}
