package com.gasmtask.challenge.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.gasmtask.challenge.config.ChallengeProperties;
import com.gasmtask.challenge.domain.ChallengePicker;
import com.gasmtask.challenge.domain.ChallengeTask;
import com.gasmtask.challenge.domain.ChallengeType;
import com.gasmtask.challenge.domain.DailyChallenge;
import com.gasmtask.challenge.dto.DailyChallengeResponse;
import com.gasmtask.challenge.repository.DailyChallengeRepository;
import com.gasmtask.economy.service.WalletService;
import com.gasmtask.planning.domain.TaskOccurrence;
import com.gasmtask.planning.repository.TaskOccurrenceRepository;
import com.gasmtask.progression.service.ProgressionService;
import com.gasmtask.progression.service.ProgressionService.XpChange;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.user.service.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Desafios diários. O sorteio acontece no primeiro acesso do dia (tela Hoje ou primeira conclusão) e fica
 * gravado. O progresso é sempre recalculado das tarefas do dia; quando um desafio é cumprido, XP e moedas
 * são pagos uma vez. As duas rotas que mexem aqui passam antes pelo lock da linha do streak, então o mesmo
 * usuário nunca sorteia ou paga em paralelo.
 */
@Service
public class ChallengeService {

    private final DailyChallengeRepository challenges;
    private final TaskOccurrenceRepository occurrences;
    private final ProgressionService progression;
    private final WalletService wallet;
    private final UserService users;
    private final UserCalendar calendar;
    private final ChallengeProperties properties;

    public ChallengeService(DailyChallengeRepository challenges, TaskOccurrenceRepository occurrences,
                            ProgressionService progression, WalletService wallet, UserService users,
                            UserCalendar calendar, ChallengeProperties properties) {
        this.challenges = challenges;
        this.occurrences = occurrences;
        this.progression = progression;
        this.wallet = wallet;
        this.users = users;
        this.calendar = calendar;
        this.properties = properties;
    }

    /** Os desafios de hoje com o progresso atual (sorteia, se ainda não sorteou). */
    @Transactional
    public List<DailyChallengeResponse> today(UUID userId) {
        Day day = dayOf(userId);
        return drawn(userId, day).stream().map(challenge -> responseOf(challenge, day)).toList();
    }

    /** Garante o sorteio de hoje antes de uma conclusão, para ela já contar no progresso. */
    @Transactional
    public void ensureToday(UUID userId) {
        drawn(userId, dayOf(userId));
    }

    /** Paga os desafios de hoje que acabaram de ser cumpridos e devolve só esses. */
    @Transactional
    public ChallengeResult evaluate(UUID userId) {
        Day day = dayOf(userId);
        List<DailyChallengeResponse> done = new ArrayList<>();
        XpChange xp = null;
        for (DailyChallenge challenge : drawn(userId, day)) {
            if (challenge.isCompleted() || challenge.getCode().progress(day.tasks()) < challenge.getTarget()) {
                continue;
            }
            challenge.complete(calendar.now());
            XpChange gained = progression.awardChallenge(userId, challenge.getId(), challenge.getXpReward());
            wallet.creditChallenge(userId, challenge.getId(), challenge.getCoinReward());
            xp = xp == null ? gained : xp.then(gained);
            done.add(responseOf(challenge, day));
        }
        return new ChallengeResult(done, Optional.ofNullable(xp));
    }

    private List<DailyChallenge> drawn(UUID userId, Day day) {
        List<DailyChallenge> existing = challenges.findByUserIdAndChallengeDate(userId, day.date());
        if (!existing.isEmpty()) {
            return existing.stream().sorted(Comparator.comparingInt(challenge -> challenge.getCode().ordinal()))
                    .toList();
        }
        // Dia sem nada possível (sem tarefas, por exemplo): nada é gravado e o sorteio é tentado de novo depois
        Instant now = calendar.now();
        return ChallengePicker.pick(userId, day.date(), day.tasks(), day.time(), properties.perDay()).stream()
                .map(type -> challenges.save(DailyChallenge.draw(userId, day.date(), type, properties.xpReward(),
                        properties.coinReward(), now)))
                .toList();
    }

    private Day dayOf(UUID userId) {
        ZoneId zone = users.timeInfo(userId).zone();
        ZonedDateTime now = calendar.now(zone);
        LocalDate today = now.toLocalDate();
        List<ChallengeTask> tasks = occurrences.findByUserIdAndOccurrenceDate(userId, today).stream()
                .map(occurrence -> taskOf(occurrence, zone))
                .toList();
        return new Day(today, now.toLocalTime(), tasks);
    }

    private static ChallengeTask taskOf(TaskOccurrence occurrence, ZoneId zone) {
        LocalTime completedAt = occurrence.getCompletedAt() == null
                ? null
                : occurrence.getCompletedAt().atZone(zone).toLocalTime();
        return new ChallengeTask(occurrence.getKind(), occurrence.getCategory(), occurrence.isPending(), completedAt,
                Boolean.TRUE.equals(occurrence.getOnTime()), occurrence.isProofAttached(),
                occurrence.getPlannedTime() != null && occurrence.plannedInAdvance(zone));
    }

    private static DailyChallengeResponse responseOf(DailyChallenge challenge, Day day) {
        ChallengeType type = challenge.getCode();
        return new DailyChallengeResponse(type, challenge.getTarget(),
                Math.min(challenge.getTarget(), type.progress(day.tasks())), challenge.isCompleted(),
                challenge.getXpReward(), challenge.getCoinReward());
    }

    private record Day(LocalDate date, LocalTime time, List<ChallengeTask> tasks) {
    }

    /**
     * @param completed desafios cumpridos agora
     * @param xp        o XP que eles renderam, se algum foi cumprido
     */
    public record ChallengeResult(List<DailyChallengeResponse> completed, Optional<XpChange> xp) {
    }
}
