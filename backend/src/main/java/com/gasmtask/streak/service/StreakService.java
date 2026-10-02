package com.gasmtask.streak.service;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import com.gasmtask.planning.domain.DayProgress;
import com.gasmtask.planning.repository.TaskOccurrenceRepository;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.streak.domain.DayStatus;
import com.gasmtask.streak.domain.Streak;
import com.gasmtask.streak.domain.StreakView;
import com.gasmtask.streak.dto.StreakResponse;
import com.gasmtask.streak.repository.DailyResultRepository;
import com.gasmtask.streak.repository.StreakRepository;
import com.gasmtask.user.service.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StreakService {

    private final StreakRepository streaks;
    private final DailyResultRepository dailyResults;
    private final TaskOccurrenceRepository occurrences;
    private final DayClosingService closing;
    private final UserService users;
    private final UserCalendar calendar;

    public StreakService(StreakRepository streaks, DailyResultRepository dailyResults,
                         TaskOccurrenceRepository occurrences, DayClosingService closing, UserService users,
                         UserCalendar calendar) {
        this.streaks = streaks;
        this.dailyResults = dailyResults;
        this.occurrences = occurrences;
        this.closing = closing;
        this.users = users;
        this.calendar = calendar;
    }

    @Transactional(readOnly = true)
    public StreakView view(UUID userId, LocalDate today, DayProgress todayProgress) {
        return streaks.findById(userId)
                .orElseGet(() -> Streak.start(userId, today))
                .view(today, todayProgress);
    }

    /** Quantos dias já fechados terminaram em cada situação (todas as situações presentes, mesmo com zero). */
    @Transactional(readOnly = true)
    public Map<DayStatus, Integer> closedDays(UUID userId) {
        Map<DayStatus, Integer> counts = new EnumMap<>(DayStatus.class);
        for (DayStatus status : DayStatus.values()) {
            counts.put(status, (int) dailyResults.countByUserIdAndStatus(userId, status));
        }
        return counts;
    }

    @Transactional
    public StreakResponse current(UUID userId) {
        closing.closePendingDays(userId);
        LocalDate today = calendar.today(users.timeInfo(userId).zone());
        DayProgress progress = DayProgress.of(occurrences.findByUserIdAndOccurrenceDate(userId, today));
        return StreakResponse.of(view(userId, today, progress));
    }
}
