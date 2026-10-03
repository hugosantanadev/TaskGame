package com.gasmtask.streak.service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gasmtask.achievement.service.AchievementService;
import com.gasmtask.planning.domain.DayProgress;
import com.gasmtask.planning.domain.TaskOccurrence;
import com.gasmtask.planning.repository.TaskOccurrenceRepository;
import com.gasmtask.planning.repository.WeeklyPlanRepository;
import com.gasmtask.progression.service.ProgressionService;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.streak.domain.DailyResult;
import com.gasmtask.streak.domain.DayStatus;
import com.gasmtask.streak.domain.Streak;
import com.gasmtask.streak.domain.StreakRules;
import com.gasmtask.streak.repository.DailyResultRepository;
import com.gasmtask.streak.repository.StreakRepository;
import com.gasmtask.user.service.UserService;
import com.gasmtask.user.service.UserTimeInfo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Virada do dia (RN16, RN22): as pendentes de cada dia passado viram perdidas (as obrigatórias custam XP),
 * o dia fecha como cumprido, falha ou descanso e o streak é atualizado. Roda no job periódico e, como garantia, antes das telas que
 * mostram o streak. Os dias fecham em ordem e uma vez só; o lock na linha do streak serializa as execuções
 * do mesmo usuário.
 */
@Service
public class DayClosingService {

    private final StreakRepository streaks;
    private final DailyResultRepository dailyResults;
    private final TaskOccurrenceRepository occurrences;
    private final WeeklyPlanRepository plans;
    private final UserService users;
    private final UserCalendar calendar;
    private final AchievementService achievements;
    private final ProgressionService progression;

    public DayClosingService(StreakRepository streaks, DailyResultRepository dailyResults,
                             TaskOccurrenceRepository occurrences, WeeklyPlanRepository plans, UserService users,
                             UserCalendar calendar, AchievementService achievements,
                             ProgressionService progression) {
        this.streaks = streaks;
        this.dailyResults = dailyResults;
        this.occurrences = occurrences;
        this.plans = plans;
        this.users = users;
        this.calendar = calendar;
        this.achievements = achievements;
        this.progression = progression;
    }

    @Transactional
    public void closePendingDays(UUID userId) {
        UserTimeInfo info = users.timeInfo(userId);
        LocalDate yesterday = calendar.today(info.zone()).minusDays(1);
        Streak streak = streaks.findForUpdate(userId)
                .orElseGet(() -> streaks.saveAndFlush(Streak.start(userId, info.registrationDate())));
        LocalDate first = streak.firstOpenDate();
        if (first.isAfter(yesterday)) {
            return;
        }

        Map<LocalDate, List<TaskOccurrence>> byDate = occurrences
                .findByUserIdAndOccurrenceDateBetween(userId, first, yesterday).stream()
                .collect(Collectors.groupingBy(TaskOccurrence::getOccurrenceDate));
        Instant now = calendar.now();
        for (LocalDate date = first; !date.isAfter(yesterday); date = date.plusDays(1)) {
            List<TaskOccurrence> day = byDate.getOrDefault(date, List.of());
            // Obrigatória que passou do dia sem ser concluída custa XP (elo ranqueado); extra perdida, não
            List<TaskOccurrence> missedMandatory = day.stream()
                    .filter(occurrence -> occurrence.isPending() && occurrence.isMandatory())
                    .toList();
            day.forEach(TaskOccurrence::markMissed);
            missedMandatory.forEach(occurrence -> progression.penalizeMissed(userId, occurrence.getId()));
            DayProgress progress = DayProgress.of(day);
            DayStatus status = StreakRules.statusOf(progress.mandatoryPlanned(), progress.mandatoryDone());
            DayStatus closed = streak.close(date, status);
            dailyResults.save(DailyResult.of(userId, date, closed, progress, streak.getCurrentStreak(), now));
        }

        // Semanas que terminaram (até o último domingo fechado) ficam marcadas como encerradas
        LocalDate lastClosedSunday = yesterday.getDayOfWeek() == DayOfWeek.SUNDAY
                ? yesterday
                : UserCalendar.weekStartOf(yesterday).minusDays(1);
        plans.closeWeeksUpTo(userId, UserCalendar.weekStartOf(lastClosedSunday), now);

        // Streak e semanas completas mudam no fechamento: as conquistas desses critérios são avaliadas aqui (RN27)
        achievements.evaluate(userId, streak.getLongestStreak());
    }
}
