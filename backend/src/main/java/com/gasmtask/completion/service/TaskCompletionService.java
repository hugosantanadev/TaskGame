package com.gasmtask.completion.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.gasmtask.achievement.service.AchievementService;
import com.gasmtask.completion.domain.Proof;
import com.gasmtask.completion.dto.CompletionResponse;
import com.gasmtask.completion.dto.ProofAttachedResponse;
import com.gasmtask.completion.dto.RewardResponse;
import com.gasmtask.completion.repository.ProofRepository;
import com.gasmtask.economy.domain.CoinTransactionReason;
import com.gasmtask.economy.domain.Reward;
import com.gasmtask.economy.domain.RewardPolicy;
import com.gasmtask.economy.service.WalletService;
import com.gasmtask.planning.domain.DayProgress;
import com.gasmtask.planning.domain.TaskOccurrence;
import com.gasmtask.planning.mapper.OccurrenceMapper;
import com.gasmtask.planning.repository.TaskOccurrenceRepository;
import com.gasmtask.progression.service.ProgressionService;
import com.gasmtask.progression.service.ProgressionService.XpChange;
import com.gasmtask.shared.exception.BusinessException;
import com.gasmtask.shared.exception.ErrorCode;
import com.gasmtask.shared.storage.FileStorage;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.streak.domain.StreakView;
import com.gasmtask.streak.service.DayClosingService;
import com.gasmtask.streak.service.StreakService;
import com.gasmtask.user.service.UserService;
import com.gasmtask.user.service.UserTimeInfo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

/**
 * Conclusão de tarefas (RN13–RN20): valida dono, dia e prova, calcula a recompensa no backend, paga cada
 * parte uma única vez e devolve o efeito no dia e no streak.
 */
@Service
public class TaskCompletionService {

    private final TaskOccurrenceRepository occurrences;
    private final ProofRepository proofs;
    private final FileStorage storage;
    private final WalletService wallet;
    private final StreakService streaks;
    private final DayClosingService closing;
    private final RewardPolicy rewards;
    private final UserService users;
    private final UserCalendar calendar;
    private final OccurrenceMapper mapper;
    private final AchievementService achievements;
    private final ProgressionService progression;

    public TaskCompletionService(TaskOccurrenceRepository occurrences, ProofRepository proofs, FileStorage storage,
                                 WalletService wallet, StreakService streaks, DayClosingService closing,
                                 RewardPolicy rewards, UserService users, UserCalendar calendar,
                                 OccurrenceMapper mapper, AchievementService achievements,
                                 ProgressionService progression) {
        this.occurrences = occurrences;
        this.proofs = proofs;
        this.storage = storage;
        this.wallet = wallet;
        this.streaks = streaks;
        this.closing = closing;
        this.rewards = rewards;
        this.users = users;
        this.calendar = calendar;
        this.mapper = mapper;
        this.achievements = achievements;
        this.progression = progression;
    }

    @Transactional
    public CompletionResponse complete(UUID userId, UUID occurrenceId, MultipartFile proofFile) {
        // O dia anterior precisa estar fechado antes de calcular o streak de hoje
        closing.closePendingDays(userId);
        UserTimeInfo info = users.timeInfo(userId);
        ZonedDateTime now = calendar.now(info.zone());
        LocalDate today = now.toLocalDate();

        TaskOccurrence occurrence = findOwned(userId, occurrenceId);
        if (!occurrence.isPending()) {
            throw new BusinessException(ErrorCode.OCCURRENCE_NOT_PENDING);
        }
        if (!occurrence.getOccurrenceDate().equals(today)) {
            throw new BusinessException(ErrorCode.NOT_COMPLETABLE_TODAY);
        }
        ProofUpload upload = ProofUpload.from(proofFile);
        if (occurrence.isRequiresProof() && upload == null) {
            throw new BusinessException(ErrorCode.PROOF_REQUIRED);
        }

        List<TaskOccurrence> todays = occurrences.findByUserIdAndOccurrenceDate(userId, today);
        boolean wasFulfilled = DayProgress.of(todays).fulfilled();

        boolean onTime = rewards.isOnTime(today, occurrence.getPlannedTime(), occurrence.getDurationMinutes(),
                occurrence.plannedInAdvance(info.zone()), now);
        Reward reward = rewards.rewardFor(occurrence.getPoints(), occurrence.getBaseCoins(), onTime, upload != null);
        occurrence.complete(now.toInstant(), onTime, reward);
        if (upload != null) {
            storeProof(userId, occurrence, upload, now.toInstant());
        }
        wallet.creditReward(userId, occurrence.getId(), CoinTransactionReason.TASK_REWARD, reward.baseCoins());
        wallet.creditReward(userId, occurrence.getId(), CoinTransactionReason.ON_TIME_BONUS, reward.onTimeBonus());
        wallet.creditReward(userId, occurrence.getId(), CoinTransactionReason.PROOF_BONUS, reward.proofBonus());

        // A ocorrência concluída é a mesma instância que está na lista de hoje
        DayProgress progress = DayProgress.of(todays);
        StreakView streak = streaks.view(userId, today, progress);
        boolean dayFulfilledNow = progress.fulfilled() && !wasFulfilled;
        XpChange xp = progression.awardTask(userId, occurrence.getId(), reward.points());
        if (dayFulfilledNow) {
            xp = xp.then(progression.awardDayFulfilled(userId, today));
        }
        return new CompletionResponse(
                mapper.toResponse(occurrence, today, false),
                onTime,
                RewardResponse.of(reward),
                wallet.balanceOf(userId),
                new CompletionResponse.Day(streak.todayStatus(), progress.mandatoryDone(), progress.mandatoryPlanned()),
                new CompletionResponse.StreakChange(streak.current(), streak.longest(), dayFulfilledNow),
                achievements.evaluate(userId, streak.longest()),
                xp.toResponse());
    }

    /** RN20: a prova pode vir depois da conclusão, até o fim do mesmo dia; o bônus é pago uma vez. */
    @Transactional
    public ProofAttachedResponse attachProof(UUID userId, UUID occurrenceId, MultipartFile proofFile) {
        UserTimeInfo info = users.timeInfo(userId);
        ZonedDateTime now = calendar.now(info.zone());
        LocalDate today = now.toLocalDate();

        TaskOccurrence occurrence = findOwned(userId, occurrenceId);
        if (!occurrence.isCompleted()) {
            throw new BusinessException(ErrorCode.REQUEST_FAILED, "Conclua a tarefa enviando a foto junto.");
        }
        if (!occurrence.getOccurrenceDate().equals(today)) {
            throw new BusinessException(ErrorCode.NOT_COMPLETABLE_TODAY, "A prova só pode ser enviada no próprio dia.");
        }
        if (occurrence.isProofAttached()) {
            throw new BusinessException(ErrorCode.PROOF_ALREADY_ATTACHED);
        }
        ProofUpload upload = ProofUpload.from(proofFile);
        if (upload == null) {
            throw new BusinessException(ErrorCode.PROOF_REQUIRED, "Envie a foto no campo \"proof\".");
        }
        storeProof(userId, occurrence, upload, now.toInstant());
        boolean paid = wallet.creditReward(userId, occurrence.getId(), CoinTransactionReason.PROOF_BONUS,
                rewards.proofBonus());
        if (paid) {
            occurrence.addEarnedCoins(rewards.proofBonus());
        }
        return new ProofAttachedResponse(mapper.toResponse(occurrence, today, false),
                paid ? rewards.proofBonus() : 0, wallet.balanceOf(userId));
    }

    @Transactional(readOnly = true)
    public ProofContent loadProof(UUID userId, UUID occurrenceId) {
        Proof proof = proofs.findByOccurrenceIdAndUserId(occurrenceId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Prova não encontrada."));
        return new ProofContent(storage.read(proof.getStorageKey()), proof.getContentType());
    }

    private TaskOccurrence findOwned(UUID userId, UUID occurrenceId) {
        return occurrences.findByIdAndUserId(occurrenceId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Tarefa não encontrada."));
    }

    private void storeProof(UUID userId, TaskOccurrence occurrence, ProofUpload upload, Instant now) {
        String key = "proofs/%s/%s.%s".formatted(userId, occurrence.getId(), upload.type().extension());
        storage.save(key, upload.bytes());
        // Se a transação falhar depois daqui, o arquivo não pode ficar órfão no disco
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    storage.delete(key);
                }
            }
        });
        proofs.save(Proof.image(occurrence.getId(), userId, key, upload.type().contentType(), upload.bytes().length,
                now));
        occurrence.markProofAttached();
    }
}
