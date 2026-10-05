-- Títulos de atributo, baú semanal e a base de planos para o app virar SaaS.

-- Título que a pessoa escolheu mostrar (ex.: "Mestre nos estudos") e o plano da conta (FREE por enquanto)
ALTER TABLE users
    ADD COLUMN active_title VARCHAR(30),
    ADD COLUMN plan         VARCHAR(10) NOT NULL DEFAULT 'FREE',
    ADD CONSTRAINT ck_users_plan CHECK (plan IN ('FREE', 'PRO'));

-- Baú semanal: o tamanho depende de quantos dias da semana anterior foram cumpridos. Um por semana.
CREATE TABLE weekly_chests (
    id             UUID        PRIMARY KEY,
    user_id        UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    week_start     DATE        NOT NULL,
    fulfilled_days INTEGER     NOT NULL,
    tier           VARCHAR(10) NOT NULL,
    coins          INTEGER     NOT NULL,
    xp             INTEGER     NOT NULL,
    item_code      VARCHAR(40),
    created_at     TIMESTAMPTZ NOT NULL,
    opened_at      TIMESTAMPTZ,
    CONSTRAINT uq_weekly_chests UNIQUE (user_id, week_start),
    CONSTRAINT ck_weekly_chests_tier CHECK (tier IN ('NONE', 'WOOD', 'SILVER', 'GOLD', 'LEGENDARY')),
    CONSTRAINT ck_weekly_chests_week CHECK (EXTRACT(ISODOW FROM week_start) = 1),
    CONSTRAINT ck_weekly_chests_values CHECK (fulfilled_days BETWEEN 0 AND 7 AND coins >= 0 AND xp >= 0)
);

-- O que o baú paga entra nos extratos de XP e de moedas, uma vez só
ALTER TABLE xp_events ADD COLUMN chest_id UUID REFERENCES weekly_chests (id) ON DELETE SET NULL;
ALTER TABLE xp_events DROP CONSTRAINT ck_xp_events_reason;
ALTER TABLE xp_events ADD CONSTRAINT ck_xp_events_reason CHECK (reason IN
    ('TASK_COMPLETED', 'DAY_FULFILLED', 'TASK_MISSED', 'BACKFILL', 'CHALLENGE_COMPLETED', 'CHEST_OPENED'));
CREATE UNIQUE INDEX uq_xp_events_chest ON xp_events (chest_id) WHERE chest_id IS NOT NULL;

ALTER TABLE coin_transactions ADD COLUMN chest_id UUID REFERENCES weekly_chests (id) ON DELETE SET NULL;
ALTER TABLE coin_transactions DROP CONSTRAINT ck_coin_transactions_reason;
ALTER TABLE coin_transactions ADD CONSTRAINT ck_coin_transactions_reason CHECK (reason IN
    ('TASK_REWARD', 'ON_TIME_BONUS', 'PROOF_BONUS', 'PURCHASE', 'CHALLENGE_REWARD', 'STREAK_FREEZE', 'CHEST_REWARD'));
CREATE UNIQUE INDEX uq_coin_transactions_chest ON coin_transactions (chest_id) WHERE chest_id IS NOT NULL;
