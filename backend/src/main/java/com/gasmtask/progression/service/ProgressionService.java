package com.gasmtask.progression.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.gasmtask.planning.domain.TaskOccurrence;
import com.gasmtask.planning.repository.TaskOccurrenceRepository;
import com.gasmtask.progression.config.ProgressionProperties;
import com.gasmtask.progression.domain.PlayerProgress;
import com.gasmtask.progression.domain.Rank;
import com.gasmtask.progression.domain.RankLadder;
import com.gasmtask.progression.domain.RankTier;
import com.gasmtask.progression.domain.XpEvent;
import com.gasmtask.progression.domain.XpReason;
import com.gasmtask.progression.dto.ProgressionResponse;
import com.gasmtask.progression.dto.RankResponse;
import com.gasmtask.progression.dto.RankStatusResponse;
import com.gasmtask.progression.dto.XpChangeResponse;
import com.gasmtask.progression.repository.PlayerProgressRepository;
import com.gasmtask.progression.repository.XpEventRepository;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.store.dto.StoreItemResponse;
import com.gasmtask.store.service.InventoryService;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Elo ranqueado: o XP sobe com cada tarefa concluída (os pontos dela) e com o bônus de dia cumprido, e desce
 * com cada obrigatória perdida. O elo é derivado do XP atual; as roupas de elo, do maior XP já alcançado,
 * então cair de elo não tira o que já foi ganho. Toda mudança passa pela linha travada do progresso.
 */
@Service
public class ProgressionService {

    private static final int RECENT_EVENTS = 20;

    private final PlayerProgressRepository progress;
    private final XpEventRepository events;
    private final TaskOccurrenceRepository occurrences;
    private final InventoryService inventory;
    private final ProgressionProperties properties;
    private final UserCalendar calendar;

    public ProgressionService(PlayerProgressRepository progress, XpEventRepository events,
                              TaskOccurrenceRepository occurrences, InventoryService inventory,
                              ProgressionProperties properties, UserCalendar calendar) {
        this.progress = progress;
        this.events = events;
        this.occurrences = occurrences;
        this.inventory = inventory;
        this.properties = properties;
        this.calendar = calendar;
    }

    /** XP de uma tarefa concluída: os pontos que ela rendeu. Uma vez por tarefa. */
    @Transactional
    public XpChange awardTask(UUID userId, UUID occurrenceId, int points) {
        if (events.existsByOccurrenceIdAndReason(occurrenceId, XpReason.TASK_COMPLETED)) {
            return XpChange.none(currentXp(userId));
        }
        return apply(userId, XpReason.TASK_COMPLETED, points, occurrenceId, null, null, null);
    }

    /** Bônus do dia cumprido. Uma vez por dia. */
    @Transactional
    public XpChange awardDayFulfilled(UUID userId, LocalDate date) {
        if (events.existsByUserIdAndEventDateAndReason(userId, date, XpReason.DAY_FULFILLED)) {
            return XpChange.none(currentXp(userId));
        }
        return apply(userId, XpReason.DAY_FULFILLED, properties.dayFulfilledBonus(), null, date, null, null);
    }

    /** Perda por obrigatória que passou do dia sem ser concluída. Uma vez por tarefa. */
    @Transactional
    public XpChange penalizeMissed(UUID userId, UUID occurrenceId) {
        if (events.existsByOccurrenceIdAndReason(occurrenceId, XpReason.TASK_MISSED)) {
            return XpChange.none(currentXp(userId));
        }
        return apply(userId, XpReason.TASK_MISSED, -properties.missedMandatoryPenalty(), occurrenceId, null, null, null);
    }

    /** XP de um desafio diário cumprido. Uma vez por desafio. */
    @Transactional
    public XpChange awardChallenge(UUID userId, UUID challengeId, int xp) {
        if (events.existsByChallengeId(challengeId)) {
            return XpChange.none(currentXp(userId));
        }
        return apply(userId, XpReason.CHALLENGE_COMPLETED, xp, null, null, challengeId, null);
    }

    @Transactional(readOnly = true)
    public RankStatusResponse status(UUID userId) {
        return RankStatusResponse.of(currentXp(userId));
    }

    /** O elo por inteiro. Entrega antes as roupas que faltarem (contas que vieram do histórico, por exemplo). */
    @Transactional
    public ProgressionResponse view(UUID userId) {
        PlayerProgress current = lock(userId);
        syncRewards(userId, current);

        List<String> codes = Arrays.stream(RankTier.values()).map(RankTier::rewardCode).filter(Objects::nonNull)
                .toList();
        Map<String, StoreItemResponse> items = inventory.itemsByCodes(userId, codes).stream()
                .collect(Collectors.toMap(StoreItemResponse::code, Function.identity()));
        List<ProgressionResponse.Reward> rewards = Arrays.stream(RankTier.values())
                .filter(tier -> tier.rewardCode() != null && items.containsKey(tier.rewardCode()))
                .map(tier -> new ProgressionResponse.Reward(tier, items.get(tier.rewardCode())))
                .toList();

        List<XpEvent> recent = events.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, RECENT_EVENTS));
        Map<UUID, String> titles = occurrences.findAllById(recent.stream().map(XpEvent::getOccurrenceId)
                        .filter(Objects::nonNull).toList()).stream()
                .collect(Collectors.toMap(TaskOccurrence::getId, TaskOccurrence::getTitle));

        return new ProgressionResponse(
                RankStatusResponse.of(current.getXp()),
                current.getPeakXp(),
                RankResponse.of(current.peakRank()),
                RankLadder.STEPS.stream()
                        .map(step -> new ProgressionResponse.Step(RankResponse.of(step), step.minXp()))
                        .toList(),
                rewards,
                recent.stream()
                        .map(event -> new ProgressionResponse.Event(event.getAmount(), event.getReason(),
                                event.getOccurrenceId() == null ? null : titles.get(event.getOccurrenceId()),
                                event.getEventDate(), event.getCreatedAt()))
                        .toList());
    }

    /** XP de um baú semanal aberto. Uma vez por baú. */
    @Transactional
    public XpChange awardChest(UUID userId, UUID chestId, int xp) {
        if (events.existsByChestId(chestId)) {
            return XpChange.none(currentXp(userId));
        }
        return apply(userId, XpReason.CHEST_OPENED, xp, null, null, null, chestId);
    }

    private XpChange apply(UUID userId, XpReason reason, int amount, UUID occurrenceId, LocalDate date,
                           UUID challengeId, UUID chestId) {
        PlayerProgress current = lock(userId);
        Rank before = current.rank();
        int applied = current.apply(amount, calendar.now());
        if (applied != 0) {
            events.save(XpEvent.of(userId, applied, reason, occurrenceId, date, challengeId, chestId, calendar.now()));
        }
        return new XpChange(applied, current.getXp(), before, current.rank(), syncRewards(userId, current));
    }

    /** Entrega as roupas dos elos já alcançados (pelo maior XP) que a pessoa ainda não tem. */
    private List<StoreItemResponse> syncRewards(UUID userId, PlayerProgress current) {
        List<String> codes = RankLadder.tiersReachedBy(current.getPeakXp()).stream()
                .map(RankTier::rewardCode)
                .filter(Objects::nonNull)
                .toList();
        return inventory.grant(userId, codes);
    }

    private PlayerProgress lock(UUID userId) {
        return progress.findForUpdate(userId)
                .orElseGet(() -> progress.saveAndFlush(PlayerProgress.start(userId, calendar.now())));
    }

    private int currentXp(UUID userId) {
        return progress.findById(userId).map(PlayerProgress::getXp).orElse(0);
    }

    /**
     * Resultado de uma ou mais mudanças de XP seguidas.
     *
     * @param before elo antes da primeira mudança
     * @param after  elo depois da última
     */
    public record XpChange(int gained, int xp, Rank before, Rank after, List<StoreItemResponse> unlocked) {

        static XpChange none(int xp) {
            Rank rank = RankLadder.rankOf(xp);
            return new XpChange(0, xp, rank, rank, List.of());
        }

        /** Junta esta mudança com a seguinte, como se fossem uma só. */
        public XpChange then(XpChange next) {
            List<StoreItemResponse> all = new ArrayList<>(unlocked);
            all.addAll(next.unlocked());
            return new XpChange(gained + next.gained(), next.xp(), before, next.after(), List.copyOf(all));
        }

        public XpChangeResponse toResponse() {
            return new XpChangeResponse(gained, RankStatusResponse.of(xp), after.isAbove(before),
                    before.isAbove(after), unlocked);
        }
    }
}
