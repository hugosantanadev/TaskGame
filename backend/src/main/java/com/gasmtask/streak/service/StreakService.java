package com.gasmtask.streak.service;

import java.time.LocalDate;
import java.util.List;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.gasmtask.economy.service.WalletService;
import com.gasmtask.planning.domain.DayProgress;
import com.gasmtask.planning.repository.TaskOccurrenceRepository;
import com.gasmtask.shared.exception.BusinessException;
import com.gasmtask.shared.exception.ErrorCode;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.streak.config.StreakProperties;
import com.gasmtask.streak.domain.DailyResult;
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
    private final WalletService wallet;
    private final StreakProperties properties;

    public StreakService(StreakRepository streaks, DailyResultRepository dailyResults,
                         TaskOccurrenceRepository occurrences, DayClosingService closing, UserService users,
                         UserCalendar calendar, WalletService wallet, StreakProperties properties) {
        this.streaks = streaks;
        this.dailyResults = dailyResults;
        this.occurrences = occurrences;
        this.closing = closing;
        this.users = users;
        this.calendar = calendar;
        this.wallet = wallet;
        this.properties = properties;
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
        return responseOf(view(userId, today, progress));
    }

    public StreakResponse responseOf(StreakView view) {
        return StreakResponse.of(view, properties);
    }

    /** Situação de cada dia já fechado no intervalo (inclusive os salvos pelo protetor). */
    @Transactional(readOnly = true)
    public Map<LocalDate, DayStatus> closedStatuses(UUID userId, LocalDate from, LocalDate to) {
        Map<LocalDate, DayStatus> statuses = new HashMap<>();
        List<DailyResult> results = dailyResults.findByUserIdAndResultDateBetweenOrderByResultDateAsc(userId, from, to);
        results.forEach(result -> statuses.put(result.getResultDate(), result.getStatus()));
        return statuses;
    }

    /**
     * Compra um protetor de sequência: debita as moedas e guarda o protetor, na mesma transação.
     * Sem saldo, nada muda (INSUFFICIENT_COINS); com o máximo guardado, a compra é recusada.
     */
    @Transactional
    public StreakResponse buyFreeze(UUID userId) {
        closing.closePendingDays(userId);
        Streak streak = streaks.findForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        if (streak.getFreezes() >= properties.maxFreezes()) {
            throw new BusinessException(ErrorCode.FREEZE_LIMIT_REACHED);
        }
        wallet.debitStreakFreeze(userId, properties.freezePrice());
        streak.addFreeze(properties.maxFreezes());
        return current(userId);
    }
}
