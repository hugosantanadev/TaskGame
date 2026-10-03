-- Protetor de sequência: comprado com moedas e guardado no streak. Na virada do dia, um dia que seria falha
-- consome um protetor e fecha como protegido (FROZEN): a sequência não zera, mas também não sobe.

ALTER TABLE streaks
    ADD COLUMN freezes          INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN last_frozen_date DATE,
    ADD CONSTRAINT ck_streaks_freezes CHECK (freezes >= 0);

ALTER TABLE daily_results DROP CONSTRAINT ck_daily_results_status;
ALTER TABLE daily_results ADD CONSTRAINT ck_daily_results_status
    CHECK (status IN ('FULFILLED', 'FAILED', 'REST', 'FROZEN'));

-- A compra do protetor sai da carteira como gasto, como as compras da loja
ALTER TABLE coin_transactions DROP CONSTRAINT ck_coin_transactions_reason;
ALTER TABLE coin_transactions ADD CONSTRAINT ck_coin_transactions_reason
    CHECK (reason IN ('TASK_REWARD', 'ON_TIME_BONUS', 'PROOF_BONUS', 'PURCHASE', 'CHALLENGE_REWARD', 'STREAK_FREEZE'));
ALTER TABLE coin_transactions DROP CONSTRAINT ck_coin_transactions_sign;
ALTER TABLE coin_transactions ADD CONSTRAINT ck_coin_transactions_sign
    CHECK ((reason IN ('PURCHASE', 'STREAK_FREEZE')) = (amount < 0));
