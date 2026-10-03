-- Elo ranqueado: XP que sobe com as tarefas concluídas e com os dias cumpridos e desce com as obrigatórias
-- perdidas. O elo (Ferro 1 a Lenda) é derivado do XP; o maior XP já alcançado guarda as roupas desbloqueadas.

CREATE TABLE player_progress (
    user_id    UUID        PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    xp         INTEGER     NOT NULL,
    peak_xp    INTEGER     NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version    BIGINT      NOT NULL,
    CONSTRAINT ck_player_progress_xp CHECK (xp >= 0 AND peak_xp >= xp)
);

-- Extrato de XP, imutável como o de moedas: cada ganho ou perda é uma linha
CREATE TABLE xp_events (
    id            UUID        PRIMARY KEY,
    user_id       UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    amount        INTEGER     NOT NULL,
    reason        VARCHAR(20) NOT NULL,
    occurrence_id UUID        REFERENCES task_occurrences (id) ON DELETE SET NULL,
    event_date    DATE,
    created_at    TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_xp_events_amount CHECK (amount <> 0),
    CONSTRAINT ck_xp_events_reason CHECK (reason IN ('TASK_COMPLETED', 'DAY_FULFILLED', 'TASK_MISSED', 'BACKFILL')),
    CONSTRAINT ck_xp_events_day CHECK ((reason = 'DAY_FULFILLED') = (event_date IS NOT NULL))
);

-- Cada tarefa rende ou custa XP uma vez só; cada dia cumprido dá o bônus uma vez só
CREATE UNIQUE INDEX uq_xp_events_occurrence_reason ON xp_events (occurrence_id, reason) WHERE occurrence_id IS NOT NULL;
CREATE UNIQUE INDEX uq_xp_events_day_reason ON xp_events (user_id, event_date, reason) WHERE event_date IS NOT NULL;
CREATE INDEX ix_xp_events_user_created ON xp_events (user_id, created_at DESC);

-- Roupas de elo: fora da loja (available = false), ganhas ao chegar no elo; por isso podem ter preço zero
ALTER TABLE store_items DROP CONSTRAINT ck_store_items_price;
ALTER TABLE store_items ADD CONSTRAINT ck_store_items_price CHECK (price > 0 OR NOT available);

INSERT INTO store_items (id, code, name, description, category, slot, price, available, asset_key, sort_order) VALUES
    (gen_random_uuid(), 'rank_bronze_headband', 'Faixa de Bronze', 'Recompensa de quem chegou ao Bronze.', 'CHARACTER', 'HEAD', 0, FALSE, 'character.rank_bronze_headband.v1', 1010),
    (gen_random_uuid(), 'rank_silver_jacket', 'Jaqueta Prata', 'Recompensa de quem chegou à Prata.', 'CHARACTER', 'OUTFIT', 0, FALSE, 'character.rank_silver_jacket.v1', 1020),
    (gen_random_uuid(), 'rank_gold_chain', 'Corrente de Ouro', 'Recompensa de quem chegou ao Ouro.', 'CHARACTER', 'ACCESSORY', 0, FALSE, 'character.rank_gold_chain.v1', 1030),
    (gen_random_uuid(), 'rank_platinum_visor', 'Viseira de Platina', 'Recompensa de quem chegou à Platina.', 'CHARACTER', 'HEAD', 0, FALSE, 'character.rank_platinum_visor.v1', 1040),
    (gen_random_uuid(), 'rank_diamond_armor', 'Armadura de Diamante', 'Recompensa de quem chegou ao Diamante.', 'CHARACTER', 'OUTFIT', 0, FALSE, 'character.rank_diamond_armor.v1', 1050),
    (gen_random_uuid(), 'rank_master_cape', 'Capa de Mestre', 'Recompensa de quem chegou ao Mestre.', 'CHARACTER', 'ACCESSORY', 0, FALSE, 'character.rank_master_cape.v1', 1060),
    (gen_random_uuid(), 'rank_legend_crown', 'Coroa da Lenda', 'Recompensa de quem virou Lenda.', 'CHARACTER', 'HEAD', 0, FALSE, 'character.rank_legend_crown.v1', 1070);

-- Contas existentes começam com o XP do histórico: pontos das concluídas + 10 por dia cumprido
-- - 5 por obrigatória perdida, nunca abaixo de zero. As roupas do elo alcançado são entregues no primeiro acesso.
INSERT INTO player_progress (user_id, xp, peak_xp, updated_at, version)
SELECT u.id, x.xp, x.xp, now(), 0
FROM users u
CROSS JOIN LATERAL (
    SELECT GREATEST(0,
        COALESCE((SELECT SUM(o.earned_points) FROM task_occurrences o
                  WHERE o.user_id = u.id AND o.status = 'COMPLETED'), 0)
        + 10 * (SELECT COUNT(*) FROM daily_results d WHERE d.user_id = u.id AND d.status = 'FULFILLED')
        - 5 * (SELECT COUNT(*) FROM task_occurrences o
               WHERE o.user_id = u.id AND o.status = 'MISSED' AND o.kind = 'MANDATORY')
    )::integer AS xp
) x;

INSERT INTO xp_events (id, user_id, amount, reason, created_at)
SELECT gen_random_uuid(), user_id, xp, 'BACKFILL', now() FROM player_progress WHERE xp > 0;
