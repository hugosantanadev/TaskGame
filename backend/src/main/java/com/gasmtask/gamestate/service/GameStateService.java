package com.gasmtask.gamestate.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.gasmtask.achievement.service.AchievementService;
import com.gasmtask.character.domain.CharacterSlot;
import com.gasmtask.character.service.CharacterService;
import com.gasmtask.economy.service.WalletService;
import com.gasmtask.gamestate.dto.GameStateResponse;
import com.gasmtask.planning.domain.OccurrenceStatus;
import com.gasmtask.planning.repository.TaskOccurrenceRepository;
import com.gasmtask.progression.service.ProgressionService;
import com.gasmtask.shared.time.TimeOfDay;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.store.dto.InventoryItemResponse;
import com.gasmtask.store.service.InventoryService;
import com.gasmtask.streak.dto.StreakResponse;
import com.gasmtask.streak.service.StreakService;
import com.gasmtask.user.service.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A fronteira com o jogo: junta o que os outros módulos já calculam (período do dia, carteira, streak,
 * coleção, quarto e personagem) num formato pensado para quem desenha, sem regra nova.
 */
@Service
public class GameStateService {

    private final StreakService streaks;
    private final WalletService wallet;
    private final InventoryService inventory;
    private final CharacterService characters;
    private final AchievementService achievements;
    private final TaskOccurrenceRepository occurrences;
    private final UserService users;
    private final UserCalendar calendar;
    private final ProgressionService progression;

    public GameStateService(StreakService streaks, WalletService wallet, InventoryService inventory,
                            CharacterService characters, AchievementService achievements,
                            TaskOccurrenceRepository occurrences, UserService users, UserCalendar calendar,
                            ProgressionService progression) {
        this.streaks = streaks;
        this.wallet = wallet;
        this.inventory = inventory;
        this.characters = characters;
        this.achievements = achievements;
        this.occurrences = occurrences;
        this.users = users;
        this.calendar = calendar;
        this.progression = progression;
    }

    @Transactional
    public GameStateResponse gameState(UUID userId) {
        StreakResponse streak = streaks.current(userId); // fecha os dias pendentes antes
        TimeOfDay timeOfDay = TimeOfDay.of(calendar.now(users.timeInfo(userId).zone()).toLocalTime());
        List<InventoryItemResponse> owned = inventory.list(userId);

        Map<CharacterSlot, GameStateResponse.Item> equipped = new EnumMap<>(CharacterSlot.class);
        owned.stream()
                .filter(item -> item.equippedSlot() != null)
                .forEach(item -> equipped.put(item.equippedSlot(), itemOf(item)));

        return new GameStateResponse(
                timeOfDay,
                wallet.balanceOf(userId),
                new GameStateResponse.StreakState(streak.current(), streak.longest(), streak.todayStatus()),
                progression.status(userId),
                new GameStateResponse.Totals(
                        (int) occurrences.countByUserIdAndStatus(userId, OccurrenceStatus.COMPLETED),
                        achievements.unlockedCount(userId)),
                owned.stream().map(GameStateService::itemOf).toList(),
                new GameStateResponse.RoomState(owned.stream()
                        .filter(InventoryItemResponse::inRoom)
                        .map(GameStateService::itemOf)
                        .toList()),
                new GameStateResponse.CharacterLook(characters.view(userId).state(), equipped));
    }

    private static GameStateResponse.Item itemOf(InventoryItemResponse owned) {
        return new GameStateResponse.Item(owned.item().code(), owned.item().assetKey());
    }
}
