package com.gasmtask.task.service;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gasmtask.economy.domain.RewardPolicy;
import com.gasmtask.planning.config.PlanningProperties;
import com.gasmtask.planning.service.PlanningService;
import com.gasmtask.shared.exception.BusinessException;
import com.gasmtask.shared.exception.ErrorCode;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.task.domain.ScheduleDistributor;
import com.gasmtask.task.domain.ScheduleSlot;
import com.gasmtask.task.domain.Task;
import com.gasmtask.task.domain.TaskChange;
import com.gasmtask.task.domain.TaskDefinition;
import com.gasmtask.task.domain.TaskKind;
import com.gasmtask.task.dto.ScheduleSuggestionResponse;
import com.gasmtask.task.dto.TaskRequest;
import com.gasmtask.task.dto.TaskResponse;
import com.gasmtask.task.mapper.TaskMapper;
import com.gasmtask.task.repository.TaskRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Missões do usuário. Toda mudança é repassada ao planejamento para o plano da semana acompanhar. */
@Service
public class TaskService {

    private static final Locale PT_BR = Locale.of("pt", "BR");

    private final TaskRepository tasks;
    private final PlanningService planning;
    private final RewardPolicy rewards;
    private final PlanningProperties limits;
    private final UserCalendar calendar;
    private final TaskMapper mapper;

    public TaskService(TaskRepository tasks, PlanningService planning, RewardPolicy rewards, PlanningProperties limits,
                       UserCalendar calendar, TaskMapper mapper) {
        this.tasks = tasks;
        this.planning = planning;
        this.rewards = rewards;
        this.limits = limits;
        this.calendar = calendar;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> list(UUID userId, boolean archived) {
        List<Task> found = archived
                ? tasks.findByUserIdAndArchivedAtIsNotNullOrderByArchivedAtDesc(userId)
                : tasks.findByUserIdAndArchivedAtIsNullOrderByCreatedAtAsc(userId);
        return found.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse get(UUID userId, UUID taskId) {
        return mapper.toResponse(find(userId, taskId));
    }

    @Transactional
    public TaskResponse create(UUID userId, TaskRequest request) {
        TaskDefinition definition = toDefinition(request);
        checkDailyLimits(userId, null, definition);
        // As semanas existem antes da missão; assim a geração abaixo respeita a escolha de incluir hoje.
        planning.ensureUpcomingWeeks(userId);
        Task task = tasks.save(Task.create(userId, definition, calendar.now()));
        planning.planNewTask(task, request.startToday());
        return mapper.toResponse(task);
    }

    @Transactional
    public TaskResponse update(UUID userId, UUID taskId, TaskRequest request) {
        Task task = find(userId, taskId);
        if (task.isArchived()) {
            throw new BusinessException(ErrorCode.TASK_ARCHIVED);
        }
        TaskDefinition definition = toDefinition(request);
        checkDailyLimits(userId, taskId, definition);
        TaskChange change = task.update(definition, calendar.now());
        planning.replanTask(task, change);
        return mapper.toResponse(task);
    }

    @Transactional
    public TaskResponse archive(UUID userId, UUID taskId) {
        Task task = find(userId, taskId);
        task.archive(calendar.now());
        planning.unplanTask(task);
        return mapper.toResponse(task);
    }

    public ScheduleSuggestionResponse suggest(int timesPerWeek) {
        if (timesPerWeek < 1 || timesPerWeek > 7) {
            throw new BusinessException(ErrorCode.REQUEST_FAILED, "Vezes por semana deve ficar entre 1 e 7.");
        }
        return new ScheduleSuggestionResponse(timesPerWeek, ScheduleDistributor.suggest(timesPerWeek));
    }

    private Task find(UUID userId, UUID taskId) {
        return tasks.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Missão não encontrada."));
    }

    private TaskDefinition toDefinition(TaskRequest request) {
        List<ScheduleSlot> slots = request.schedule().stream()
                .map(entry -> new ScheduleSlot(entry.dayOfWeek(), entry.time()))
                .toList();
        if (slots.stream().map(ScheduleSlot::day).distinct().count() != slots.size()) {
            throw new BusinessException(ErrorCode.INVALID_SCHEDULE);
        }
        int points;
        if (request.kind() == TaskKind.MANDATORY) {
            points = rewards.mandatoryPoints();
        } else {
            points = request.points() == null ? rewards.extraMinPoints() : request.points();
            if (!rewards.acceptsExtraPoints(points)) {
                throw new BusinessException(ErrorCode.REQUEST_FAILED, "Pontos de extra fora da faixa permitida.");
            }
        }
        String description = request.description() == null || request.description().isBlank()
                ? null
                : request.description().strip();
        return new TaskDefinition(request.name().strip(), description, request.category(), request.kind(), points,
                request.durationMinutes(), request.requiresProof(), slots);
    }

    /** RN12: com a missão nova ou editada, nenhum dia da semana passa do teto do tipo dela. */
    private void checkDailyLimits(UUID userId, UUID editedTaskId, TaskDefinition definition) {
        int limit = limits.limitFor(definition.kind());
        Map<DayOfWeek, Long> perDay = tasks.findByUserIdAndArchivedAtIsNullOrderByCreatedAtAsc(userId).stream()
                .filter(task -> !task.getId().equals(editedTaskId) && task.getKind() == definition.kind())
                .flatMap(task -> task.slots().stream())
                .collect(Collectors.groupingBy(ScheduleSlot::day, Collectors.counting()));
        for (ScheduleSlot slot : definition.slots()) {
            if (perDay.getOrDefault(slot.day(), 0L) + 1 > limit) {
                throw new BusinessException(ErrorCode.DAILY_LIMIT_REACHED, "Limite de %d %s por dia atingido na %s."
                        .formatted(limit, definition.kind() == TaskKind.MANDATORY ? "obrigatórias" : "extras",
                                slot.day().getDisplayName(TextStyle.FULL, PT_BR)));
            }
        }
    }
}
