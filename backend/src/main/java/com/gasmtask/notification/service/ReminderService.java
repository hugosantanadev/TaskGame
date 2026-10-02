package com.gasmtask.notification.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.gasmtask.notification.domain.ReminderPlanner;
import com.gasmtask.notification.domain.ReminderPlanner.PlannedTask;
import com.gasmtask.notification.domain.ReminderPlanner.ReminderPreferences;
import com.gasmtask.notification.dto.UpcomingReminderResponse;
import com.gasmtask.planning.domain.TaskOccurrence;
import com.gasmtask.planning.repository.TaskOccurrenceRepository;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.user.dto.ReminderSettingsResponse;
import com.gasmtask.user.service.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Próximos lembretes do usuário. No MVP o app os agenda enquanto está aberto; quando o Web Push chegar,
 * um job usa o mesmo cálculo para enviar com o app fechado.
 */
@Service
public class ReminderService {

    static final int MAX_HOURS = 48;

    private final TaskOccurrenceRepository occurrences;
    private final UserService users;
    private final UserCalendar calendar;

    public ReminderService(TaskOccurrenceRepository occurrences, UserService users, UserCalendar calendar) {
        this.occurrences = occurrences;
        this.users = users;
        this.calendar = calendar;
    }

    @Transactional(readOnly = true)
    public List<UpcomingReminderResponse> upcoming(UUID userId, int hours) {
        ZonedDateTime now = calendar.now(users.timeInfo(userId).zone());
        Duration window = Duration.ofHours(Math.clamp(hours, 1, MAX_HOURS));
        ReminderSettingsResponse settings = users.reminderSettings(userId);
        // Até o dia seguinte ao fim da janela: uma tarefa logo depois da meia-noite pode avisar antes dela
        LocalDate lastDay = now.plus(window).toLocalDate().plusDays(1);
        List<PlannedTask> tasks = occurrences
                .findByUserIdAndOccurrenceDateBetween(userId, now.toLocalDate(), lastDay).stream()
                .filter(TaskOccurrence::isPending)
                .filter(occurrence -> occurrence.getPlannedTime() != null)
                .map(occurrence -> new PlannedTask(occurrence.getId(), occurrence.getTitle(),
                        occurrence.getOccurrenceDate(), occurrence.getPlannedTime()))
                .toList();
        return ReminderPlanner.plan(now, window, new ReminderPreferences(settings.tasksEnabled(),
                        settings.leadMinutes(), settings.bedtime(), settings.wakeTime()), tasks).stream()
                .map(UpcomingReminderResponse::of)
                .toList();
    }
}
