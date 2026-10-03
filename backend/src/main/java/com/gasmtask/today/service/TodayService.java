package com.gasmtask.today.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.gasmtask.challenge.service.ChallengeService;
import com.gasmtask.economy.domain.RewardPolicy;
import com.gasmtask.economy.service.WalletService;
import com.gasmtask.planning.domain.DayProgress;
import com.gasmtask.planning.domain.TaskOccurrence;
import com.gasmtask.planning.mapper.OccurrenceMapper;
import com.gasmtask.planning.repository.TaskOccurrenceRepository;
import com.gasmtask.planning.service.PlanningService;
import com.gasmtask.progression.service.ProgressionService;
import com.gasmtask.shared.time.TimeOfDay;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.streak.dto.StreakResponse;
import com.gasmtask.streak.service.DayClosingService;
import com.gasmtask.streak.service.StreakService;
import com.gasmtask.today.dto.ProgressResponse;
import com.gasmtask.today.dto.TodayResponse;
import com.gasmtask.user.service.UserService;
import com.gasmtask.user.service.UserTimeInfo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TodayService {

    private final DayClosingService closing;
    private final PlanningService planning;
    private final TaskOccurrenceRepository occurrences;
    private final StreakService streaks;
    private final WalletService wallet;
    private final RewardPolicy rewards;
    private final UserService users;
    private final UserCalendar calendar;
    private final OccurrenceMapper mapper;
    private final ProgressionService progression;
    private final ChallengeService challenges;

    public TodayService(DayClosingService closing, PlanningService planning, TaskOccurrenceRepository occurrences,
                        StreakService streaks, WalletService wallet, RewardPolicy rewards, UserService users,
                        UserCalendar calendar, OccurrenceMapper mapper, ProgressionService progression,
                        ChallengeService challenges) {
        this.closing = closing;
        this.planning = planning;
        this.occurrences = occurrences;
        this.streaks = streaks;
        this.wallet = wallet;
        this.rewards = rewards;
        this.users = users;
        this.calendar = calendar;
        this.mapper = mapper;
        this.progression = progression;
        this.challenges = challenges;
    }

    @Transactional
    public TodayResponse today(UUID userId) {
        closing.closePendingDays(userId);
        UserTimeInfo info = users.timeInfo(userId);
        planning.ensureUpcomingWeeks(userId, info.zone());
        ZonedDateTime now = calendar.now(info.zone());
        LocalDate today = now.toLocalDate();
        boolean onboarding = planning.isOnboardingOpen(userId, today, info.registrationDate());

        List<TaskOccurrence> list = occurrences.findByUserIdAndOccurrenceDate(userId, today).stream()
                .sorted(PlanningService.byTime())
                .toList();
        DayProgress progress = DayProgress.of(list);
        return new TodayResponse(
                today,
                TimeOfDay.of(now.toLocalTime()),
                list.stream().map(occurrence -> mapper.toResponse(occurrence, today, onboarding)).toList(),
                nextOf(list, now.toLocalTime()).map(TaskOccurrence::getId).orElse(null),
                ProgressResponse.of(progress),
                wallet.balanceOf(userId),
                StreakResponse.of(streaks.view(userId, today, progress)),
                onboarding,
                progression.status(userId),
                challenges.today(userId));
    }

    /**
     * Próxima tarefa: a primeira pendente com horário cuja janela ainda não acabou; senão a primeira sem horário;
     * senão qualquer pendente (atrasada, mas ainda dá para concluir hoje).
     */
    private Optional<TaskOccurrence> nextOf(List<TaskOccurrence> sorted, LocalTime now) {
        int nowMinutes = now.getHour() * 60 + now.getMinute();
        int tolerance = (int) rewards.onTimeTolerance().toMinutes();
        List<TaskOccurrence> pending = sorted.stream().filter(TaskOccurrence::isPending).toList();
        return pending.stream()
                .filter(occurrence -> occurrence.getPlannedTime() != null)
                .filter(occurrence -> windowEndMinutes(occurrence, tolerance) >= nowMinutes)
                .findFirst()
                .or(() -> pending.stream().filter(occurrence -> occurrence.getPlannedTime() == null).findFirst())
                .or(() -> pending.stream().findFirst());
    }

    private static int windowEndMinutes(TaskOccurrence occurrence, int tolerance) {
        LocalTime time = occurrence.getPlannedTime();
        int duration = occurrence.getDurationMinutes() == null ? 0 : occurrence.getDurationMinutes();
        return time.getHour() * 60 + time.getMinute() + duration + tolerance;
    }
}
