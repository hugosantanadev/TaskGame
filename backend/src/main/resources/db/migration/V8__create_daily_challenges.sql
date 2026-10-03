-- Desafios diários: três por dia, sorteados entre os que o plano do dia permite cumprir e gravados no primeiro
-- acesso. O progresso sai das tarefas do dia; a recompensa (XP e moedas) é paga uma vez por desafio.

CREATE TABLE daily_challenges (
    id             UUID        PRIMARY KEY,
    user_id        UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    challenge_date DATE        NOT NULL,
    code           VARCHAR(20) NOT NULL,
    target         INTEGER     NOT NULL,
    xp_reward      INTEGER     NOT NULL,
    coin_reward    INTEGER     NOT NULL,
    completed_at   TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_daily_challenges UNIQUE (user_id, challenge_date, code),
    CONSTRAINT ck_daily_challenges_code CHECK (code IN
        ('EARLY_BIRD', 'ON_TIME', 'PHOTO', 'EXTRA_MILE', 'VARIETY', 'FULL_DAY', 'MARATHON')),
    CONSTRAINT ck_daily_challenges_values CHECK (target > 0 AND xp_reward >= 0 AND coin_reward >= 0)
);

-- A recompensa entra nos extratos de XP e de moedas apontando para o desafio, uma vez só
ALTER TABLE xp_events ADD COLUMN challenge_id UUID REFERENCES daily_challenges (id) ON DELETE SET NULL;
ALTER TABLE xp_events DROP CONSTRAINT ck_xp_events_reason;
ALTER TABLE xp_events ADD CONSTRAINT ck_xp_events_reason
    CHECK (reason IN ('TASK_COMPLETED', 'DAY_FULFILLED', 'TASK_MISSED', 'BACKFILL', 'CHALLENGE_COMPLETED'));
CREATE UNIQUE INDEX uq_xp_events_challenge ON xp_events (challenge_id) WHERE challenge_id IS NOT NULL;

ALTER TABLE coin_transactions ADD COLUMN challenge_id UUID REFERENCES daily_challenges (id) ON DELETE SET NULL;
ALTER TABLE coin_transactions DROP CONSTRAINT ck_coin_transactions_reason;
ALTER TABLE coin_transactions ADD CONSTRAINT ck_coin_transactions_reason
    CHECK (reason IN ('TASK_REWARD', 'ON_TIME_BONUS', 'PROOF_BONUS', 'PURCHASE', 'CHALLENGE_REWARD'));
CREATE UNIQUE INDEX uq_coin_transactions_challenge ON coin_transactions (challenge_id) WHERE challenge_id IS NOT NULL;
