package com.gasmtask.chest.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import com.gasmtask.chest.domain.ChestTier;
import com.gasmtask.chest.domain.WeeklyChest;
import com.gasmtask.chest.dto.ChestResponse;
import com.gasmtask.chest.dto.OpenedChestResponse;
import com.gasmtask.chest.repository.WeeklyChestRepository;
import com.gasmtask.economy.service.WalletService;
import com.gasmtask.progression.service.ProgressionService;
import com.gasmtask.progression.service.ProgressionService.XpChange;
import com.gasmtask.shared.exception.BusinessException;
import com.gasmtask.shared.exception.ErrorCode;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.store.dto.StoreItemResponse;
import com.gasmtask.store.service.InventoryService;
import com.gasmtask.store.service.StoreService;
import com.gasmtask.streak.domain.DayStatus;
import com.gasmtask.streak.service.DayClosingService;
import com.gasmtask.streak.service.StreakService;
import com.gasmtask.user.service.UserService;
import com.gasmtask.user.service.UserTimeInfo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Baú semanal: na primeira visita depois do domingo, a semana anterior vira um baú do tamanho dos dias cumpridos
 * (2 ou mais). Abrir paga moedas e XP, e o ouro e o lendário entregam um item da loja que a pessoa não tem.
 * O conteúdo é decidido quando o baú nasce, com sorteio fixo por pessoa e semana.
 */
@Service
public class ChestService {

    private final WeeklyChestRepository chests;
    private final DayClosingService closing;
    private final StreakService streaks;
    private final StoreService store;
    private final InventoryService inventory;
    private final WalletService wallet;
    private final ProgressionService progression;
    private final UserService users;
    private final UserCalendar calendar;

    public ChestService(WeeklyChestRepository chests, DayClosingService closing, StreakService streaks,
                        StoreService store, InventoryService inventory, WalletService wallet,
                        ProgressionService progression, UserService users, UserCalendar calendar) {
        this.chests = chests;
        this.closing = closing;
        this.streaks = streaks;
        this.store = store;
        this.inventory = inventory;
        this.wallet = wallet;
        this.progression = progression;
        this.users = users;
        this.calendar = calendar;
    }

    /** O baú mais antigo ainda fechado (cria o da semana passada, se for a hora). */
    @Transactional
    public Optional<ChestResponse> pending(UUID userId) {
        ensureLastWeek(userId);
        return chests.findFirstByUserIdAndOpenedAtIsNullAndTierNotOrderByWeekStartAsc(userId, ChestTier.NONE)
                .map(ChestResponse::of);
    }

    @Transactional
    public List<ChestResponse> history(UUID userId) {
        ensureLastWeek(userId);
        return chests.findByUserIdAndTierNotOrderByWeekStartDesc(userId, ChestTier.NONE).stream()
                .map(ChestResponse::of)
                .toList();
    }

    @Transactional
    public OpenedChestResponse open(UUID userId, UUID chestId) {
        WeeklyChest chest = chests.findForUpdate(chestId, userId)
                .filter(WeeklyChest::hasReward)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Baú não encontrado."));
        if (chest.isOpened()) {
            throw new BusinessException(ErrorCode.CHEST_ALREADY_OPENED);
        }
        chest.open(calendar.now());

        StoreItemResponse item = null;
        int coins = chest.getCoins();
        if (chest.getItemCode() != null) {
            List<StoreItemResponse> granted = inventory.grant(userId, List.of(chest.getItemCode()));
            if (granted.isEmpty()) {
                coins += chest.getTier().itemFallbackCoins(); // comprou o item antes de abrir: vira moedas
            } else {
                item = granted.getFirst();
            }
        }
        wallet.creditChest(userId, chest.getId(), coins);
        XpChange xp = progression.awardChest(userId, chest.getId(), chest.getXp());
        return new OpenedChestResponse(ChestResponse.of(chest), coins, item, xp.toResponse(),
                wallet.balanceOf(userId));
    }

    /**
     * Cria o baú da semana passada uma vez, se a pessoa já existia nela. Passa antes pelo fechamento dos dias,
     * que trava a linha do streak: duas visitas ao mesmo tempo não criam o baú duas vezes.
     */
    private void ensureLastWeek(UUID userId) {
        closing.closePendingDays(userId);
        UserTimeInfo info = users.timeInfo(userId);
        LocalDate lastWeek = UserCalendar.weekStartOf(calendar.today(info.zone())).minusWeeks(1);
        LocalDate lastSunday = lastWeek.plusDays(6);
        if (info.registrationDate().isAfter(lastSunday) || chests.existsByUserIdAndWeekStart(userId, lastWeek)) {
            return;
        }
        int fulfilled = (int) streaks.closedStatuses(userId, lastWeek, lastSunday).values().stream()
                .filter(status -> status == DayStatus.FULFILLED)
                .count();
        ChestTier tier = ChestTier.forFulfilledDays(fulfilled);
        String item = tier.maxItemPrice() > 0 ? pickItem(userId, lastWeek, tier) : null;
        chests.save(WeeklyChest.of(userId, lastWeek, fulfilled, item, calendar.now()));
    }

    /** Um item da loja que a pessoa ainda não tem, até o preço do baú; sorteio fixo por pessoa e semana. */
    private String pickItem(UUID userId, LocalDate weekStart, ChestTier tier) {
        List<StoreItemResponse> candidates = store.catalog(userId, null).stream()
                .filter(item -> !item.owned() && item.price() <= tier.maxItemPrice())
                .toList();
        if (candidates.isEmpty()) {
            return null;
        }
        Random random = new Random(userId.getLeastSignificantBits() ^ weekStart.toEpochDay());
        return candidates.get(random.nextInt(candidates.size())).code();
    }
}
