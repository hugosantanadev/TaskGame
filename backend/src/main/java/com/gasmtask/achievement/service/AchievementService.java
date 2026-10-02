package com.gasmtask.achievement.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.gasmtask.achievement.domain.Achievement;
import com.gasmtask.achievement.domain.AchievementMetrics;
import com.gasmtask.achievement.domain.UserAchievement;
import com.gasmtask.achievement.dto.AchievementResponse;
import com.gasmtask.achievement.dto.UnlockedAchievementResponse;
import com.gasmtask.achievement.repository.AchievementRepository;
import com.gasmtask.achievement.repository.UserAchievementRepository;
import com.gasmtask.planning.domain.OccurrenceStatus;
import com.gasmtask.planning.repository.CategoryCount;
import com.gasmtask.planning.repository.TaskOccurrenceRepository;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.user.service.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Avalia as conquistas (RN27) depois de cada conclusão e no fechamento dos dias. As avaliações do mesmo
 * usuário não correm em paralelo: as duas rotas passam antes pelo lock da linha do streak.
 */
@Service
public class AchievementService {

    private final AchievementRepository achievements;
    private final UserAchievementRepository unlocked;
    private final TaskOccurrenceRepository occurrences;
    private final UserService users;
    private final UserCalendar calendar;

    public AchievementService(AchievementRepository achievements, UserAchievementRepository unlocked,
                              TaskOccurrenceRepository occurrences, UserService users, UserCalendar calendar) {
        this.achievements = achievements;
        this.unlocked = unlocked;
        this.occurrences = occurrences;
        this.users = users;
        this.calendar = calendar;
    }

    /** Desbloqueia o que foi alcançado e ainda não estava desbloqueado; devolve só as novas. */
    @Transactional
    public List<UnlockedAchievementResponse> evaluate(UUID userId, int longestStreak) {
        AchievementMetrics metrics = metrics(userId, longestStreak);
        var already = unlocked.findByUserId(userId).stream().map(UserAchievement::getAchievementId)
                .collect(Collectors.toSet());
        Instant now = calendar.now();
        List<UnlockedAchievementResponse> fresh = new ArrayList<>();
        for (Achievement achievement : achievements.findAllByOrderBySortOrderAsc()) {
            if (!already.contains(achievement.getId()) && achievement.isReachedBy(metrics)) {
                unlocked.save(UserAchievement.unlock(userId, achievement.getId(), now));
                fresh.add(new UnlockedAchievementResponse(achievement.getCode(), achievement.getName(),
                        achievement.getDescription()));
            }
        }
        return fresh;
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> list(UUID userId, int longestStreak) {
        AchievementMetrics metrics = metrics(userId, longestStreak);
        Map<UUID, UserAchievement> byAchievement = unlocked.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserAchievement::getAchievementId, Function.identity()));
        return achievements.findAllByOrderBySortOrderAsc().stream()
                .map(achievement -> {
                    UserAchievement done = byAchievement.get(achievement.getId());
                    return new AchievementResponse(achievement.getCode(), achievement.getName(),
                            achievement.getDescription(), achievement.getCriterion(), achievement.getCategory(),
                            achievement.getThreshold(),
                            done != null ? achievement.getThreshold() : achievement.progressOf(metrics),
                            done != null, done != null ? done.getUnlockedAt() : null, achievement.getAssetKey());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public int unlockedCount(UUID userId) {
        return (int) unlocked.countByUserId(userId);
    }

    /** Conquistas desbloqueadas no intervalo [from, to), da mais antiga à mais recente. */
    @Transactional(readOnly = true)
    public List<UnlockedAchievementResponse> unlockedBetween(UUID userId, Instant from, Instant to) {
        List<UserAchievement> inRange = unlocked
                .findByUserIdAndUnlockedAtGreaterThanEqualAndUnlockedAtLessThanOrderByUnlockedAtAsc(userId, from, to);
        Map<UUID, Achievement> catalog = achievements.findAllById(
                        inRange.stream().map(UserAchievement::getAchievementId).toList()).stream()
                .collect(Collectors.toMap(Achievement::getId, Function.identity()));
        return inRange.stream()
                .map(done -> catalog.get(done.getAchievementId()))
                .map(achievement -> new UnlockedAchievementResponse(achievement.getCode(), achievement.getName(),
                        achievement.getDescription()))
                .toList();
    }

    private AchievementMetrics metrics(UUID userId, int longestStreak) {
        int total = (int) occurrences.countByUserIdAndStatus(userId, OccurrenceStatus.COMPLETED);
        var byCategory = occurrences.countByCategory(userId, OccurrenceStatus.COMPLETED).stream()
                .collect(Collectors.toMap(CategoryCount::getCategory, count -> (int) count.getTotal()));
        int sameTask = occurrences.countPerTask(userId, OccurrenceStatus.COMPLETED).stream()
                .mapToInt(Long::intValue).max().orElse(0);
        LocalDate currentWeek = UserCalendar.weekStartOf(calendar.today(users.timeInfo(userId).zone()));
        int completeWeeks = (int) occurrences.countCompleteWeeks(userId, currentWeek);
        return new AchievementMetrics(total, byCategory, sameTask, longestStreak, completeWeeks);
    }
}
