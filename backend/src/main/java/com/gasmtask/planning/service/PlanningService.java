package com.gasmtask.planning.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gasmtask.economy.domain.RewardPolicy;
import com.gasmtask.planning.config.PlanningProperties;
import com.gasmtask.planning.domain.DayLockPolicy;
import com.gasmtask.planning.domain.OccurrenceStatus;
import com.gasmtask.planning.domain.TaskOccurrence;
import com.gasmtask.planning.domain.WeeklyPlan;
import com.gasmtask.planning.dto.AddOccurrenceRequest;
import com.gasmtask.planning.dto.CreateExtraRequest;
import com.gasmtask.planning.dto.DayPlanResponse;
import com.gasmtask.planning.dto.MoveOccurrenceRequest;
import com.gasmtask.planning.dto.OccurrenceResponse;
import com.gasmtask.planning.dto.WeekResponse;
import com.gasmtask.planning.mapper.OccurrenceMapper;
import com.gasmtask.planning.repository.TaskOccurrenceRepository;
import com.gasmtask.planning.repository.WeeklyPlanRepository;
import com.gasmtask.shared.exception.BusinessException;
import com.gasmtask.shared.exception.ErrorCode;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.task.domain.ScheduleSlot;
import com.gasmtask.task.domain.Task;
import com.gasmtask.task.domain.TaskChange;
import com.gasmtask.task.domain.TaskKind;
import com.gasmtask.task.repository.TaskRepository;
import com.gasmtask.user.service.UserService;
import com.gasmtask.user.service.UserTimeInfo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Plano semanal: gera as ocorrências a partir das missões (RN08), mantém o plano em dia quando uma missão
 * muda (RN09) e aplica os ajustes manuais respeitando o dia congelado (RN02) e o teto diário (RN12).
 * Só a semana atual e a próxima aceitam planejamento.
 */
@Service
public class PlanningService {

    private static final Comparator<TaskOccurrence> BY_TIME = Comparator
            .comparing(TaskOccurrence::getPlannedTime, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(TaskOccurrence::getTitle);

    private final WeeklyPlanRepository plans;
    private final TaskOccurrenceRepository occurrences;
    private final TaskRepository tasks;
    private final UserService users;
    private final UserCalendar calendar;
    private final RewardPolicy rewards;
    private final PlanningProperties limits;
    private final OccurrenceMapper mapper;

    public PlanningService(WeeklyPlanRepository plans, TaskOccurrenceRepository occurrences, TaskRepository tasks,
                           UserService users, UserCalendar calendar, RewardPolicy rewards, PlanningProperties limits,
                           OccurrenceMapper mapper) {
        this.plans = plans;
        this.occurrences = occurrences;
        this.tasks = tasks;
        this.users = users;
        this.calendar = calendar;
        this.rewards = rewards;
        this.limits = limits;
        this.mapper = mapper;
    }

    public static Comparator<TaskOccurrence> byTime() {
        return BY_TIME;
    }

    // ------------------------------------------------------------------ geração

    /** Garante a semana atual e a próxima (RN08). Idempotente: chamar de novo não duplica nada. */
    @Transactional
    public void ensureUpcomingWeeks(UUID userId) {
        ensureUpcomingWeeks(userId, users.timeInfo(userId).zone());
    }

    @Transactional
    public void ensureUpcomingWeeks(UUID userId, ZoneId zone) {
        LocalDate today = calendar.today(zone);
        LocalDate currentWeek = UserCalendar.weekStartOf(today);
        ensureWeek(userId, currentWeek, today);
        ensureWeek(userId, currentWeek.plusWeeks(1), today);
    }

    private WeeklyPlan ensureWeek(UUID userId, LocalDate weekStart, LocalDate today) {
        var existing = plans.findByUserIdAndWeekStart(userId, weekStart);
        if (existing.isPresent()) {
            return existing.get();
        }
        boolean created = plans.insertIfAbsent(UUID.randomUUID(), userId, weekStart, calendar.now()) == 1;
        WeeklyPlan plan = plans.findByUserIdAndWeekStart(userId, weekStart).orElseThrow();
        if (created) {
            LocalDate from = weekStart.isAfter(today) ? weekStart : today;
            for (Task task : tasks.findByUserIdAndArchivedAtIsNullOrderByCreatedAtAsc(userId)) {
                generate(task, plan, from);
            }
        }
        return plan;
    }

    /** Cria as ocorrências da missão nos dias da semana a partir de {@code from}, sem duplicar e sem passar do teto. */
    private void generate(Task task, WeeklyPlan plan, LocalDate from) {
        LocalDate start = from.isAfter(plan.getWeekStart()) ? from : plan.getWeekStart();
        for (LocalDate date = start; !date.isAfter(plan.weekEnd()); date = date.plusDays(1)) {
            var slot = task.slotOn(date.getDayOfWeek());
            if (slot.isEmpty() || occurrences.existsByTaskIdAndOccurrenceDate(task.getId(), date)
                    || !hasRoom(task.getUserId(), date, task.getKind())) {
                continue;
            }
            occurrences.save(TaskOccurrence.fromTask(task, plan, date, slot.get().time(),
                    rewards.baseCoinsFor(task.getKind()), calendar.now()));
        }
    }

    // ------------------------------------------------------------------ sincronização com as missões

    /** Missão nova entra a partir de amanhã, ou já hoje quando o usuário pede e a regra do dia permite. */
    @Transactional
    public void planNewTask(Task task, boolean includeToday) {
        Context context = context(task.getUserId());
        boolean today = includeToday
                && DayLockPolicy.canPlace(task.getKind(), context.today(), context.today(), context.onboardingOpen());
        LocalDate from = today ? context.today() : context.today().plusDays(1);
        generate(task, context.current(), from);
        generate(task, context.next(), from);
    }

    /** RN09: a edição vale a partir de amanhã; hoje e o passado ficam como estavam. */
    @Transactional
    public void replanTask(Task task, TaskChange change) {
        Context context = context(task.getUserId());
        if (change.titleChanged()) {
            occurrences.findByTaskIdAndStatusAndOccurrenceDateGreaterThanEqual(task.getId(), OccurrenceStatus.PENDING,
                    context.today()).forEach(occurrence -> occurrence.rename(task.getName()));
        }
        if (change.planChanged()) {
            LocalDate tomorrow = context.today().plusDays(1);
            removePendingFrom(task, tomorrow);
            generate(task, context.current(), tomorrow);
            generate(task, context.next(), tomorrow);
        }
    }

    /** Missão arquivada sai do plano a partir de amanhã. */
    @Transactional
    public void unplanTask(Task task) {
        Context context = context(task.getUserId());
        removePendingFrom(task, context.today().plusDays(1));
    }

    private void removePendingFrom(Task task, LocalDate from) {
        occurrences.deleteAll(occurrences.findByTaskIdAndStatusAndOccurrenceDateGreaterThanEqual(
                task.getId(), OccurrenceStatus.PENDING, from));
        // Sem o flush, o Hibernate inseriria as novas ocorrências antes de apagar as antigas
        // e violaria a restrição única (missão, dia).
        occurrences.flush();
    }

    // ------------------------------------------------------------------ leitura

    @Transactional
    public WeekResponse week(UUID userId, LocalDate weekStart) {
        if (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new BusinessException(ErrorCode.INVALID_WEEK_START);
        }
        Context context = context(userId);
        Map<LocalDate, List<TaskOccurrence>> byDate = occurrences
                .findByUserIdAndOccurrenceDateBetween(userId, weekStart, weekStart.plusDays(6)).stream()
                .collect(Collectors.groupingBy(TaskOccurrence::getOccurrenceDate));
        boolean editable = weekStart.equals(context.current().getWeekStart())
                || weekStart.equals(context.next().getWeekStart());

        List<DayPlanResponse> days = new ArrayList<>();
        for (LocalDate date = weekStart; !date.isAfter(weekStart.plusDays(6)); date = date.plusDays(1)) {
            List<OccurrenceResponse> list = byDate.getOrDefault(date, List.of()).stream()
                    .sorted(BY_TIME)
                    .map(occurrence -> mapper.toResponse(occurrence, context.today(), context.onboardingOpen()))
                    .toList();
            days.add(new DayPlanResponse(date, date.getDayOfWeek(), date.equals(context.today()),
                    date.isBefore(context.today()),
                    editable && DayLockPolicy.canPlace(TaskKind.MANDATORY, date, context.today(), context.onboardingOpen()),
                    editable && DayLockPolicy.canPlace(TaskKind.EXTRA, date, context.today(), context.onboardingOpen()),
                    list));
        }
        return new WeekResponse(weekStart, weekStart.plusDays(6), weekStart.equals(context.current().getWeekStart()),
                editable, days);
    }

    @Transactional
    public WeekResponse currentWeek(UUID userId, int weeksAhead) {
        LocalDate today = calendar.today(users.timeInfo(userId).zone());
        return week(userId, UserCalendar.weekStartOf(today).plusWeeks(weeksAhead));
    }

    // ------------------------------------------------------------------ ajustes manuais

    @Transactional
    public OccurrenceResponse addOccurrence(UUID userId, LocalDate weekStart, AddOccurrenceRequest request) {
        Context context = context(userId);
        WeeklyPlan plan = planFor(context, request.date());
        if (!plan.getWeekStart().equals(weekStart)) {
            throw new BusinessException(ErrorCode.REQUEST_FAILED, "A data não pertence a essa semana.");
        }
        Task task = tasks.findByIdAndUserId(request.taskId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Missão não encontrada."));
        if (task.isArchived()) {
            throw new BusinessException(ErrorCode.TASK_ARCHIVED);
        }
        requirePlaceable(task.getKind(), request.date(), context);
        if (occurrences.existsByTaskIdAndOccurrenceDate(task.getId(), request.date())) {
            throw new BusinessException(ErrorCode.OCCURRENCE_ALREADY_ON_DATE);
        }
        requireRoom(userId, request.date(), task.getKind());
        LocalTime time = request.time() != null
                ? request.time()
                : task.slotOn(request.date().getDayOfWeek()).map(ScheduleSlot::time).orElse(null);
        TaskOccurrence occurrence = occurrences.save(TaskOccurrence.fromTask(task, plan, request.date(), time,
                rewards.baseCoinsFor(task.getKind()), calendar.now()));
        return mapper.toResponse(occurrence, context.today(), context.onboardingOpen());
    }

    @Transactional
    public OccurrenceResponse createExtra(UUID userId, CreateExtraRequest request) {
        Context context = context(userId);
        WeeklyPlan plan = planFor(context, request.date());
        requirePlaceable(TaskKind.EXTRA, request.date(), context);
        if (!rewards.acceptsExtraPoints(request.points())) {
            throw new BusinessException(ErrorCode.REQUEST_FAILED, "Pontos de extra fora da faixa permitida.");
        }
        requireRoom(userId, request.date(), TaskKind.EXTRA);
        TaskOccurrence occurrence = occurrences.save(TaskOccurrence.extra(userId, plan, request.title().strip(),
                request.category(), request.points(), rewards.baseCoinsFor(TaskKind.EXTRA), request.date(),
                request.time(), request.durationMinutes(), request.requiresProof(), calendar.now()));
        return mapper.toResponse(occurrence, context.today(), context.onboardingOpen());
    }

    @Transactional
    public OccurrenceResponse move(UUID userId, UUID occurrenceId, MoveOccurrenceRequest request) {
        Context context = context(userId);
        TaskOccurrence occurrence = findOwned(userId, occurrenceId);
        requireRemovable(occurrence, context);
        WeeklyPlan plan = planFor(context, request.date());
        boolean dateChanged = !request.date().equals(occurrence.getOccurrenceDate());
        if (dateChanged) {
            requirePlaceable(occurrence.getKind(), request.date(), context);
            if (occurrence.getTaskId() != null
                    && occurrences.existsByTaskIdAndOccurrenceDate(occurrence.getTaskId(), request.date())) {
                throw new BusinessException(ErrorCode.OCCURRENCE_ALREADY_ON_DATE);
            }
            requireRoom(userId, request.date(), occurrence.getKind());
        }
        occurrence.moveTo(plan.getId(), request.date(), request.time());
        return mapper.toResponse(occurrence, context.today(), context.onboardingOpen());
    }

    @Transactional
    public void remove(UUID userId, UUID occurrenceId) {
        Context context = context(userId);
        TaskOccurrence occurrence = findOwned(userId, occurrenceId);
        requireRemovable(occurrence, context);
        occurrences.delete(occurrence);
    }

    // ------------------------------------------------------------------ apoio

    /** RN03: no dia do cadastro, hoje fica editável até a primeira conclusão. */
    public boolean isOnboardingOpen(UUID userId, LocalDate today, LocalDate registrationDate) {
        return today.equals(registrationDate)
                && !occurrences.existsByUserIdAndOccurrenceDateAndStatus(userId, today, OccurrenceStatus.COMPLETED);
    }

    private Context context(UUID userId) {
        UserTimeInfo info = users.timeInfo(userId);
        LocalDate today = calendar.today(info.zone());
        ensureUpcomingWeeks(userId, info.zone());
        LocalDate currentWeek = UserCalendar.weekStartOf(today);
        WeeklyPlan current = plans.findByUserIdAndWeekStart(userId, currentWeek).orElseThrow();
        WeeklyPlan next = plans.findByUserIdAndWeekStart(userId, currentWeek.plusWeeks(1)).orElseThrow();
        return new Context(today, isOnboardingOpen(userId, today, info.registrationDate()), current, next);
    }

    private WeeklyPlan planFor(Context context, LocalDate date) {
        if (context.current().contains(date)) {
            return context.current();
        }
        if (context.next().contains(date)) {
            return context.next();
        }
        throw new BusinessException(ErrorCode.WEEK_NOT_AVAILABLE);
    }

    private TaskOccurrence findOwned(UUID userId, UUID occurrenceId) {
        return occurrences.findByIdAndUserId(occurrenceId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Tarefa não encontrada."));
    }

    private void requirePlaceable(TaskKind kind, LocalDate date, Context context) {
        if (!DayLockPolicy.canPlace(kind, date, context.today(), context.onboardingOpen())) {
            String detail = kind == TaskKind.EXTRA && !date.isBefore(context.today())
                    ? "Extras só podem ser planejadas a partir de amanhã."
                    : ErrorCode.DAY_LOCKED.defaultMessage();
            throw new BusinessException(ErrorCode.DAY_LOCKED, detail);
        }
    }

    private void requireRemovable(TaskOccurrence occurrence, Context context) {
        if (!occurrence.isPending()) {
            throw new BusinessException(ErrorCode.OCCURRENCE_NOT_PENDING);
        }
        if (!DayLockPolicy.canTakeOut(occurrence.getOccurrenceDate(), context.today(), context.onboardingOpen())) {
            throw new BusinessException(ErrorCode.DAY_LOCKED);
        }
    }

    private boolean hasRoom(UUID userId, LocalDate date, TaskKind kind) {
        return occurrences.countByUserIdAndOccurrenceDateAndKind(userId, date, kind) < limits.limitFor(kind);
    }

    private void requireRoom(UUID userId, LocalDate date, TaskKind kind) {
        if (!hasRoom(userId, date, kind)) {
            throw new BusinessException(ErrorCode.DAILY_LIMIT_REACHED,
                    "Esse dia já tem o máximo de %d %s.".formatted(limits.limitFor(kind),
                            kind == TaskKind.MANDATORY ? "obrigatórias" : "extras"));
        }
    }

    private record Context(LocalDate today, boolean onboardingOpen, WeeklyPlan current, WeeklyPlan next) {
    }
}
