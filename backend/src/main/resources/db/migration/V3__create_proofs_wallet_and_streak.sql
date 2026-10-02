-- Provas, carteira com extrato, streak e fechamento diário (Fase 2).

CREATE TABLE proofs (
    id            UUID         PRIMARY KEY,
    occurrence_id UUID         NOT NULL UNIQUE REFERENCES task_occurrences (id) ON DELETE CASCADE,
    user_id       UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    kind          VARCHAR(10)  NOT NULL,
    storage_key   VARCHAR(200) NOT NULL,
    content_type  VARCHAR(50)  NOT NULL,
    size_bytes    INTEGER      NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    CONSTRAINT ck_proofs_kind CHECK (kind IN ('IMAGE'))
);

-- RN21: o saldo nunca fica negativo
CREATE TABLE wallets (
    user_id      UUID        PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    balance      INTEGER     NOT NULL,
    total_earned INTEGER     NOT NULL,
    total_spent  INTEGER     NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL,
    version      BIGINT      NOT NULL,
    CONSTRAINT ck_wallets_balance CHECK (balance >= 0)
);

-- Extrato imutável: toda movimentação de moedas vira uma linha
CREATE TABLE coin_transactions (
    id            UUID        PRIMARY KEY,
    user_id       UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    amount        INTEGER     NOT NULL,
    reason        VARCHAR(20) NOT NULL,
    occurrence_id UUID        REFERENCES task_occurrences (id) ON DELETE SET NULL,
    created_at    TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_coin_transactions_amount CHECK (amount <> 0),
    CONSTRAINT ck_coin_transactions_reason CHECK (reason IN ('TASK_REWARD', 'ON_TIME_BONUS', 'PROOF_BONUS', 'PURCHASE'))
);

-- RN19: cada tipo de recompensa é pago no máximo uma vez por ocorrência
CREATE UNIQUE INDEX uq_coin_transactions_occurrence_reason
    ON coin_transactions (occurrence_id, reason) WHERE occurrence_id IS NOT NULL;
CREATE INDEX ix_coin_transactions_user_created ON coin_transactions (user_id, created_at DESC);

-- current_streak conta só dias já fechados; o dia de hoje é somado na leitura, quando cumprido
CREATE TABLE streaks (
    user_id             UUID    PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    current_streak      INTEGER NOT NULL,
    longest_streak      INTEGER NOT NULL,
    last_fulfilled_date DATE,
    last_closed_date    DATE,
    tracking_start_date DATE    NOT NULL,
    version             BIGINT  NOT NULL
);

CREATE TABLE daily_results (
    id                UUID        PRIMARY KEY,
    user_id           UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    result_date       DATE        NOT NULL,
    status            VARCHAR(10) NOT NULL,
    mandatory_planned INTEGER     NOT NULL,
    mandatory_done    INTEGER     NOT NULL,
    extras_done       INTEGER     NOT NULL,
    points            INTEGER     NOT NULL,
    coins             INTEGER     NOT NULL,
    streak_after      INTEGER     NOT NULL,
    closed_at         TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_daily_results_user_date UNIQUE (user_id, result_date),
    CONSTRAINT ck_daily_results_status CHECK (status IN ('FULFILLED', 'FAILED', 'REST'))
);

-- Contas criadas na Fase 1 ganham carteira e streak; o acompanhamento começa hoje, no fuso de cada um
INSERT INTO wallets (user_id, balance, total_earned, total_spent, updated_at, version)
SELECT id, 0, 0, 0, now(), 0 FROM users;

INSERT INTO streaks (user_id, current_streak, longest_streak, tracking_start_date, version)
SELECT id, 0, 0, (now() AT TIME ZONE time_zone)::date, 0 FROM users;
