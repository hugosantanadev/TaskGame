package com.gasmtask.demo;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.sql.DataSource;

import com.gasmtask.character.domain.CharacterSlot;
import com.gasmtask.character.service.CharacterService;
import com.gasmtask.progression.service.ProgressionService;
import com.gasmtask.room.service.RoomService;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.store.dto.PurchaseResponse;
import com.gasmtask.store.dto.StoreItemResponse;
import com.gasmtask.store.service.StoreService;
import com.gasmtask.streak.service.DayClosingService;
import com.gasmtask.streak.service.StreakService;
import com.gasmtask.task.domain.TaskCategory;
import com.gasmtask.task.domain.TaskKind;
import com.gasmtask.task.dto.ScheduleEntry;
import com.gasmtask.task.dto.TaskRequest;
import com.gasmtask.task.service.TaskService;
import com.gasmtask.user.dto.UserResponse;
import com.gasmtask.user.service.NewUser;
import com.gasmtask.user.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Conta de demonstração do perfil {@code dev}: seis semanas de histórico, elo, atributos, conquistas, sequência,
 * coleção montada e um protetor guardado, mais quatro pessoas para o ranking não ficar vazio.
 * <p>
 * O que dá para fazer como um usuário faria passa pelos services (cadastro, missões, compras). O histórico
 * passado não dá, porque o relógio da aplicação só conhece o agora: as ocorrências e os lançamentos antigos
 * entram direto no banco, e o fechamento do dia (o mesmo do job) calcula perdas, sequência e conquistas.
 * Roda uma vez: se a conta demo já existe, não faz nada. O resultado varia pouco entre execuções (semente fixa).
 */
@Component
@Profile("dev")
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final int DAY_FULFILLED_XP = 10;

    private static final List<Player> RIVALS = List.of(
            new Player("Ana Souza", "ana.demo@gasmtask.app", 34, 0.55, 11),
            new Player("Bruno Lima", "bruno.demo@gasmtask.app", 27, 0.45, 12),
            new Player("Carla Mendes", "carla.demo@gasmtask.app", 41, 0.70, 13),
            new Player("Diego Rocha", "diego.demo@gasmtask.app", 20, 0.35, 14));

    private final UserService users;
    private final PasswordEncoder passwordEncoder;
    private final TaskService tasks;
    private final DayClosingService closing;
    private final ProgressionService progression;
    private final StoreService store;
    private final RoomService rooms;
    private final CharacterService characters;
    private final StreakService streaks;
    private final UserCalendar calendar;
    private final JdbcTemplate jdbc;
    private final String email;
    private final String password;

    public DemoDataSeeder(UserService users, PasswordEncoder passwordEncoder, TaskService tasks,
                          DayClosingService closing, ProgressionService progression, StoreService store,
                          RoomService rooms, CharacterService characters, StreakService streaks,
                          UserCalendar calendar, DataSource dataSource,
                          @Value("${app.demo.email:demo@gasmtask.app}") String email,
                          @Value("${app.demo.password:demo1234}") String password) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tasks = tasks;
        this.closing = closing;
        this.progression = progression;
        this.store = store;
        this.rooms = rooms;
        this.characters = characters;
        this.streaks = streaks;
        this.calendar = calendar;
        this.jdbc = new JdbcTemplate(dataSource);
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.findCredentials(email).isPresent()) {
            log.info("Conta demo já existe ({}); nada a fazer", email);
            return;
        }
        UUID demo = seedPlayer(new Player("Demo", email, 41, 0.62, 7), password);
        decorate(demo);
        for (Player rival : RIVALS) {
            seedPlayer(rival, UUID.randomUUID().toString());
        }
        log.info("Conta demo criada: {} (senha {})", email, password);
    }

    private UUID seedPlayer(Player player, String rawPassword) {
        UserResponse user = users.register(new NewUser(player.email(), passwordEncoder.encode(rawPassword),
                player.name(), ZONE.getId()));
        UUID userId = user.id();
        for (TaskRequest mission : missions()) {
            tasks.create(userId, mission);
        }
        backfill(userId, player);
        closing.closePendingDays(userId);   // perdas, sequência, dias fechados e conquistas
        progression.view(userId);           // entrega as roupas dos elos alcançados
        return userId;
    }

    /** A rotina da demo: estudo nos dias úteis, Bíblia todo dia, academia três vezes e leitura à noite. */
    private static List<TaskRequest> missions() {
        List<DayOfWeek> weekdays = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        return List.of(
                mission("Estudar Java", TaskCategory.STUDY, weekdays, LocalTime.of(8, 0), 60),
                mission("Bíblia", TaskCategory.SPIRITUALITY, List.of(DayOfWeek.values()), LocalTime.of(21, 0), 20),
                mission("Academia", TaskCategory.EXERCISE,
                        List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), LocalTime.of(18, 0), 60),
                mission("Ler 20 páginas", TaskCategory.READING,
                        List.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY), LocalTime.of(22, 0), 30),
                mission("Projeto pessoal", TaskCategory.PROJECT, List.of(DayOfWeek.SATURDAY), LocalTime.of(10, 0),
                        120));
    }

    private static TaskRequest mission(String name, TaskCategory category, List<DayOfWeek> days, LocalTime time,
                                       int minutes) {
        return new TaskRequest(name, null, category, TaskKind.MANDATORY, null, minutes, false,
                days.stream().map(day -> new ScheduleEntry(day, time)).toList(), true);
    }

    /**
     * Gera o passado: ocorrências de cada missão desde o "cadastro", concluídas com chance que melhora com o
     * tempo (a pessoa pega o ritmo), com moedas e XP lançados na data em que aconteceram.
     */
    private void backfill(UUID userId, Player player) {
        LocalDate today = calendar.today(ZONE);
        LocalDate start = today.minusDays(player.daysAgo());
        Random random = new Random(player.seed());
        Instant createdAt = start.atTime(7, 30).atZone(ZONE).toInstant();
        jdbc.update("UPDATE users SET created_at = ? WHERE id = ?", Timestamp.from(createdAt), userId);
        jdbc.update("UPDATE streaks SET tracking_start_date = ? WHERE user_id = ?", start, userId);

        Map<LocalDate, UUID> plans = plansFrom(userId, start, today);
        List<Schedule> schedules = jdbc.query("""
                        SELECT t.id, t.name, t.category, t.points, t.duration_minutes, s.day_of_week, s.planned_time
                        FROM tasks t JOIN task_schedules s ON s.task_id = t.id WHERE t.user_id = ?
                        """,
                (rs, row) -> new Schedule(UUID.fromString(rs.getString("id")), rs.getString("name"),
                        rs.getString("category"), rs.getInt("points"), rs.getInt("duration_minutes"),
                        DayOfWeek.valueOf(rs.getString("day_of_week")), rs.getObject("planned_time", LocalTime.class)),
                userId);

        int coins = 0;
        int xp = 0;
        long totalDays = Math.max(1, start.until(today).getDays());
        for (LocalDate date = start; date.isBefore(today); date = date.plusDays(1)) {
            double chance = Math.min(0.95, player.rate() + 0.3 * start.until(date).getDays() / totalDays);
            int planned = 0;
            int done = 0;
            for (Schedule schedule : schedules) {
                if (schedule.day() != date.getDayOfWeek()) {
                    continue;
                }
                planned++;
                boolean completed = random.nextDouble() < chance;
                UUID occurrenceId = UUID.randomUUID();
                Instant completedAt = null;
                boolean onTime = false;
                if (completed) {
                    done++;
                    int offset = random.nextInt(-20, 50);
                    ZonedDateTime when = date.atTime(schedule.time()).plusMinutes(offset).atZone(ZONE);
                    completedAt = when.toInstant();
                    onTime = offset <= schedule.minutes() + 30;
                }
                insertOccurrence(userId, plans.get(UserCalendar.weekStartOf(date)), schedule, date, occurrenceId,
                        completedAt, onTime, start);
                if (completed) {
                    int earned = 3 + (onTime ? 1 : 0);
                    coins += earned;
                    xp += schedule.points();
                    coin(userId, occurrenceId, "TASK_REWARD", 3, completedAt);
                    if (onTime) {
                        coin(userId, occurrenceId, "ON_TIME_BONUS", 1, completedAt);
                    }
                    xpEvent(userId, schedule.points(), "TASK_COMPLETED", occurrenceId, null, completedAt);
                }
            }
            if (planned > 0 && done == planned) {
                xp += DAY_FULFILLED_XP;
                xpEvent(userId, DAY_FULFILLED_XP, "DAY_FULFILLED", null, date,
                        date.atTime(23, 0).atZone(ZONE).toInstant());
            }
        }
        jdbc.update("UPDATE wallets SET balance = balance + ?, total_earned = total_earned + ? WHERE user_id = ?",
                coins, coins, userId);
        jdbc.update("UPDATE player_progress SET xp = xp + ?, peak_xp = GREATEST(peak_xp, xp + ?) WHERE user_id = ?",
                xp, xp, userId);
    }

    /** Semanas do passado que ainda não existem; as duas atuais o cadastro já criou. */
    private Map<LocalDate, UUID> plansFrom(UUID userId, LocalDate start, LocalDate today) {
        LocalDate first = UserCalendar.weekStartOf(start);
        LocalDate current = UserCalendar.weekStartOf(today);
        for (LocalDate week = first; week.isBefore(current); week = week.plusWeeks(1)) {
            jdbc.update("""
                    INSERT INTO weekly_plans (id, user_id, week_start, generated_at)
                    VALUES (?, ?, ?, ?) ON CONFLICT (user_id, week_start) DO NOTHING
                    """, UUID.randomUUID(), userId, week, Timestamp.from(calendar.now()));
        }
        Map<LocalDate, UUID> plans = new HashMap<>();
        jdbc.query("SELECT id, week_start FROM weekly_plans WHERE user_id = ?",
                rs -> {
                    plans.put(rs.getObject("week_start", LocalDate.class), UUID.fromString(rs.getString("id")));
                }, userId);
        return plans;
    }

    private void insertOccurrence(UUID userId, UUID planId, Schedule schedule, LocalDate date, UUID id,
                                  Instant completedAt, boolean onTime, LocalDate start) {
        boolean completed = completedAt != null;
        // Planejada antes do próprio dia (vale para o bônus de horário); pendente vira perdida no fechamento
        Instant plannedAt = start.minusDays(1).atStartOfDay(ZONE).toInstant();
        jdbc.update("""
                        INSERT INTO task_occurrences (id, user_id, plan_id, task_id, occurrence_date, planned_time, title,
                            category, kind, points, base_coins, duration_minutes, requires_proof, status, completed_at,
                            on_time, earned_points, earned_coins, proof_attached, created_at, version)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'MANDATORY', ?, 3, ?, FALSE, ?, ?, ?, ?, ?, FALSE, ?, 0)
                        """,
                id, userId, planId, schedule.taskId(), date, schedule.time(), schedule.name(), schedule.category(),
                schedule.points(), schedule.minutes(), completed ? "COMPLETED" : "PENDING",
                completed ? Timestamp.from(completedAt) : null, completed ? onTime : null,
                completed ? schedule.points() : null, completed ? 3 + (onTime ? 1 : 0) : null,
                Timestamp.from(plannedAt));
    }

    private void coin(UUID userId, UUID occurrenceId, String reason, int amount, Instant at) {
        jdbc.update("""
                INSERT INTO coin_transactions (id, user_id, amount, reason, occurrence_id, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID(), userId, amount, reason, occurrenceId, Timestamp.from(at));
    }

    private void xpEvent(UUID userId, int amount, String reason, UUID occurrenceId, LocalDate date, Instant at) {
        jdbc.update("""
                INSERT INTO xp_events (id, user_id, amount, reason, occurrence_id, event_date, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID(), userId, amount, reason, occurrenceId, date, Timestamp.from(at));
    }

    /** Quarto montado, personagem vestido e um protetor guardado, como alguém que já usa o app há semanas. */
    private void decorate(UUID userId) {
        Map<String, StoreItemResponse> catalog = new HashMap<>();
        store.catalog(userId, null).forEach(item -> catalog.put(item.code(), item));
        for (String code : List.of("desk_folding", "computer_old_laptop", "bed_single")) {
            store.purchase(userId, catalog.get(code).id());
        }
        List<UUID> roomItems = new ArrayList<>();
        for (String code : List.of("lamp_desk", "plant_small", "poster_space", "mug_coffee")) {
            PurchaseResponse bought = store.purchase(userId, catalog.get(code).id());
            roomItems.add(bought.inventoryItem().id());
        }
        roomItems.forEach(item -> rooms.place(userId, item));
        PurchaseResponse shirt = store.purchase(userId, catalog.get("tshirt_stripes").id());
        characters.equip(userId, CharacterSlot.OUTFIT, shirt.inventoryItem().id());
        streaks.buyFreeze(userId);
    }

    private record Player(String name, String email, int daysAgo, double rate, long seed) {
    }

    private record Schedule(UUID taskId, String name, String category, int points, int minutes, DayOfWeek day,
                            LocalTime time) {
    }
}
