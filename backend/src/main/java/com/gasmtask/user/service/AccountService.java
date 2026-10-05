package com.gasmtask.user.service;

import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import com.gasmtask.shared.exception.BusinessException;
import com.gasmtask.shared.exception.ErrorCode;
import com.gasmtask.shared.storage.FileStorage;
import com.gasmtask.user.config.PlanProperties;
import com.gasmtask.user.domain.User;
import com.gasmtask.user.repository.UserRepository;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * A conta como produto (LGPD e SaaS): exportar todos os dados, excluir a conta e os limites do plano.
 */
@Service
public class AccountService {

    /**
     * Tudo o que é da pessoa, num JSON só, montado pelo próprio PostgreSQL. Sem hash de senha nem de tokens,
     * e as fotos de prova vão só como metadados.
     */
    private static final String EXPORT = """
            SELECT json_build_object(
              'profile', (SELECT row_to_json(x) FROM (
                  SELECT id, email, display_name, time_zone, ranking_visible, active_title, plan,
                         reminder_tasks_enabled, reminder_lead_minutes, bedtime, wake_time, created_at
                  FROM users WHERE id = :id) x),
              'missions', (SELECT coalesce(json_agg(x), '[]') FROM (
                  SELECT t.*, (SELECT coalesce(json_agg(s), '[]') FROM (
                      SELECT day_of_week, planned_time FROM task_schedules WHERE task_id = t.id) s) AS schedule
                  FROM tasks t WHERE t.user_id = :id ORDER BY t.created_at) x),
              'occurrences', (SELECT coalesce(json_agg(x), '[]') FROM (
                  SELECT * FROM task_occurrences WHERE user_id = :id ORDER BY occurrence_date) x),
              'proofs', (SELECT coalesce(json_agg(x), '[]') FROM (
                  SELECT occurrence_id, content_type, size_bytes, created_at FROM proofs WHERE user_id = :id) x),
              'wallet', (SELECT row_to_json(x) FROM (
                  SELECT balance, total_earned, total_spent FROM wallets WHERE user_id = :id) x),
              'coinTransactions', (SELECT coalesce(json_agg(x), '[]') FROM (
                  SELECT * FROM coin_transactions WHERE user_id = :id ORDER BY created_at) x),
              'xp', (SELECT row_to_json(x) FROM (
                  SELECT xp, peak_xp FROM player_progress WHERE user_id = :id) x),
              'xpEvents', (SELECT coalesce(json_agg(x), '[]') FROM (
                  SELECT * FROM xp_events WHERE user_id = :id ORDER BY created_at) x),
              'streak', (SELECT row_to_json(x) FROM (
                  SELECT current_streak, longest_streak, freezes, tracking_start_date FROM streaks WHERE user_id = :id) x),
              'dailyResults', (SELECT coalesce(json_agg(x), '[]') FROM (
                  SELECT * FROM daily_results WHERE user_id = :id ORDER BY result_date) x),
              'achievements', (SELECT coalesce(json_agg(x), '[]') FROM (
                  SELECT a.code, ua.unlocked_at FROM user_achievements ua
                  JOIN achievements a ON a.id = ua.achievement_id WHERE ua.user_id = :id) x),
              'collection', (SELECT coalesce(json_agg(x), '[]') FROM (
                  SELECT si.code, i.price_paid, i.acquired_at FROM inventory_items i
                  JOIN store_items si ON si.id = i.store_item_id WHERE i.user_id = :id) x),
              'challenges', (SELECT coalesce(json_agg(x), '[]') FROM (
                  SELECT * FROM daily_challenges WHERE user_id = :id ORDER BY challenge_date) x),
              'chests', (SELECT coalesce(json_agg(x), '[]') FROM (
                  SELECT * FROM weekly_chests WHERE user_id = :id ORDER BY week_start) x)
            )::text
            """;

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final FileStorage storage;
    private final PlanProperties plans;
    private final NamedParameterJdbcTemplate jdbc;

    public AccountService(UserRepository users, PasswordEncoder passwordEncoder, FileStorage storage,
                          PlanProperties plans, DataSource dataSource) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.storage = storage;
        this.plans = plans;
        this.jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transactional(readOnly = true)
    public String export(UUID userId) {
        return jdbc.queryForObject(EXPORT, Map.of("id", userId), String.class);
    }

    /**
     * Exclui a conta e tudo o que é dela (o banco apaga em cascata). Pede a senha de novo, para um celular
     * esquecido aberto não bastar. As fotos saem do disco só depois que a exclusão no banco der certo.
     */
    @Transactional
    public void delete(UUID userId, String password) {
        User user = users.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        if (password == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
        users.delete(user);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                storage.deleteFolder("proofs/" + userId);
            }
        });
    }

    /** Recusa uma missão nova se o plano já está no limite de missões ativas. */
    @Transactional(readOnly = true)
    public void requireRoomForMission(UUID userId, long activeMissions) {
        User user = users.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        if (!plans.of(user.getPlan()).allowsMoreMissions(activeMissions)) {
            throw new BusinessException(ErrorCode.PLAN_LIMIT_REACHED,
                    "Seu plano permite %d missões ativas. Arquive uma para criar outra."
                            .formatted(plans.of(user.getPlan()).maxActiveMissions()));
        }
    }
}
