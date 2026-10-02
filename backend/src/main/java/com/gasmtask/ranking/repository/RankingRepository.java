package com.gasmtask.ranking.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import com.gasmtask.ranking.domain.RankingMetric;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Consultas do ranking. É uma leitura que cruza usuários, ocorrências e streaks, então fica em SQL direto,
 * sem entidade própria. Todas partem do mesmo placar ({@code scores}): um valor por usuário na métrica pedida.
 * Só aparece quem escolheu aparecer e pontuou; empates dividem a posição (1, 1, 3).
 */
@Repository
public class RankingRepository {

    /** Métricas da semana: somam as ocorrências concluídas no intervalo de datas. */
    private static final String PERIOD_SCORES = """
            WITH scores AS (
                SELECT u.id AS user_id, u.display_name, u.ranking_visible, CAST(%s AS integer) AS value
                FROM users u
                LEFT JOIN task_occurrences o
                    ON o.user_id = u.id AND o.status = 'COMPLETED' AND o.occurrence_date BETWEEN :from AND :to
                GROUP BY u.id, u.display_name, u.ranking_visible
            )""";

    /**
     * Sequência atual: os dias fechados mais hoje, se hoje já está cumprido (todas as obrigatórias de hoje
     * concluídas), com "hoje" no fuso de cada usuário. É a mesma conta do Streak.view.
     */
    private static final String STREAK_SCORES = """
            WITH scores AS (
                SELECT u.id AS user_id, u.display_name, u.ranking_visible,
                    s.current_streak + CASE
                        WHEN (s.last_closed_date IS NULL OR d.today > s.last_closed_date)
                            AND t.planned > 0 AND t.done = t.planned THEN 1
                        ELSE 0 END AS value
                FROM users u
                JOIN streaks s ON s.user_id = u.id
                CROSS JOIN LATERAL (SELECT CAST(CAST(:now AS timestamptz) AT TIME ZONE u.time_zone AS date) AS today) d
                CROSS JOIN LATERAL (
                    SELECT count(*) AS planned, count(*) FILTER (WHERE o.status = 'COMPLETED') AS done
                    FROM task_occurrences o
                    WHERE o.user_id = u.id AND o.kind = 'MANDATORY' AND o.occurrence_date = d.today
                ) t
            )""";

    private static final String RANKED = """
            , ranked AS (
                SELECT user_id, display_name, value, RANK() OVER (ORDER BY value DESC) AS position
                FROM scores
                WHERE ranking_visible AND value > 0
            )
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public RankingRepository(DataSource dataSource) {
        this.jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    public List<RankedRow> page(RankingMetric metric, LocalDate from, LocalDate to, Instant now, UUID me,
                                int limit, long offset) {
        String sql = ranked(metric) + """
                SELECT position, display_name, value, user_id = :me AS you
                FROM ranked
                ORDER BY position, display_name, user_id
                LIMIT :limit OFFSET :offset
                """;
        return jdbc.query(sql, params(from, to, now, me).addValue("limit", limit).addValue("offset", offset),
                (rs, row) -> new RankedRow(rs.getInt("position"), rs.getString("display_name"), rs.getInt("value"),
                        rs.getBoolean("you")));
    }

    public long count(RankingMetric metric, LocalDate from, LocalDate to, Instant now) {
        Long total = jdbc.queryForObject(ranked(metric) + "SELECT count(*) FROM ranked",
                params(from, to, now, null), Long.class);
        return total == null ? 0 : total;
    }

    /** O placar de quem consulta, apareça ou não na lista: a posição é onde estaria entre os visíveis. */
    public Optional<MyScore> mine(RankingMetric metric, LocalDate from, LocalDate to, Instant now, UUID me) {
        String sql = ranked(metric) + """
                SELECT s.value, s.ranking_visible,
                    (SELECT count(*) FROM ranked r WHERE r.value > s.value AND r.user_id <> s.user_id) + 1 AS position
                FROM scores s
                WHERE s.user_id = :me
                """;
        return jdbc.query(sql, params(from, to, now, me),
                (rs, row) -> new MyScore(rs.getInt("value"), rs.getBoolean("ranking_visible"), rs.getInt("position")))
                .stream().findFirst();
    }

    private static String ranked(RankingMetric metric) {
        String scores = switch (metric) {
            case POINTS -> PERIOD_SCORES.formatted("COALESCE(SUM(o.earned_points), 0)");
            case COMPLETED_TASKS -> PERIOD_SCORES.formatted("COUNT(o.id)");
            case COINS_EARNED -> PERIOD_SCORES.formatted("COALESCE(SUM(o.earned_coins), 0)");
            case STREAK -> STREAK_SCORES;
        };
        return scores + RANKED;
    }

    private static MapSqlParameterSource params(LocalDate from, LocalDate to, Instant now, UUID me) {
        return new MapSqlParameterSource()
                .addValue("from", from)
                .addValue("to", to)
                .addValue("now", OffsetDateTime.ofInstant(now, ZoneOffset.UTC))
                .addValue("me", me);
    }

    public record RankedRow(int position, String displayName, int value, boolean you) {
    }

    /** @param position onde estaria entre os visíveis (1 + quantos têm valor maior) */
    public record MyScore(int value, boolean visible, int position) {
    }
}
