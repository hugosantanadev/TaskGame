-- Missões, recorrência, planos semanais e ocorrências (Fase 2).
-- Inteiros são INTEGER (e não SMALLINT) porque o Hibernate valida o tipo exato das colunas.

CREATE TABLE tasks (
    id               UUID         PRIMARY KEY,
    user_id          UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name             VARCHAR(60)  NOT NULL,
    description      VARCHAR(280),
    category         VARCHAR(20)  NOT NULL,
    kind             VARCHAR(10)  NOT NULL,
    points           INTEGER      NOT NULL,
    duration_minutes INTEGER,
    requires_proof   BOOLEAN      NOT NULL,
    archived_at      TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL,
    version          BIGINT       NOT NULL,
    CONSTRAINT ck_tasks_kind CHECK (kind IN ('MANDATORY', 'EXTRA')),
    CONSTRAINT ck_tasks_category CHECK (category IN
        ('STUDY', 'READING', 'SPIRITUALITY', 'EXERCISE', 'SLEEP', 'PROJECT', 'HOME', 'OTHER')),
    CONSTRAINT ck_tasks_points CHECK (points BETWEEN 1 AND 100),
    CONSTRAINT ck_tasks_duration CHECK (duration_minutes IS NULL OR duration_minutes BETWEEN 1 AND 720)
);

CREATE INDEX ix_tasks_user ON tasks (user_id);

-- RN07: no máximo um horário por dia da semana em cada missão
CREATE TABLE task_schedules (
    id           UUID        PRIMARY KEY,
    task_id      UUID        NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    day_of_week  VARCHAR(9)  NOT NULL,
    planned_time TIME,
    CONSTRAINT uq_task_schedules_task_day UNIQUE (task_id, day_of_week),
    CONSTRAINT ck_task_schedules_day CHECK (day_of_week IN
        ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'))
);

-- RN08: uma linha por semana do usuário; week_start é sempre uma segunda-feira
CREATE TABLE weekly_plans (
    id           UUID        PRIMARY KEY,
    user_id      UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    week_start   DATE        NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL,
    closed_at    TIMESTAMPTZ,
    CONSTRAINT uq_weekly_plans_user_week UNIQUE (user_id, week_start),
    CONSTRAINT ck_weekly_plans_monday CHECK (EXTRACT(ISODOW FROM week_start) = 1)
);

-- Ocorrência = uma missão num dia. Guarda um retrato (snapshot) do que valia quando foi planejada (RN10).
-- Extras avulsas não têm missão: task_id fica nulo.
CREATE TABLE task_occurrences (
    id               UUID         PRIMARY KEY,
    user_id          UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    plan_id          UUID         NOT NULL REFERENCES weekly_plans (id) ON DELETE CASCADE,
    task_id          UUID         REFERENCES tasks (id) ON DELETE SET NULL,
    occurrence_date  DATE         NOT NULL,
    planned_time     TIME,
    title            VARCHAR(60)  NOT NULL,
    category         VARCHAR(20)  NOT NULL,
    kind             VARCHAR(10)  NOT NULL,
    points           INTEGER      NOT NULL,
    base_coins       INTEGER      NOT NULL,
    duration_minutes INTEGER,
    requires_proof   BOOLEAN      NOT NULL,
    status           VARCHAR(10)  NOT NULL,
    completed_at     TIMESTAMPTZ,
    on_time          BOOLEAN,
    earned_points    INTEGER,
    earned_coins     INTEGER,
    proof_attached   BOOLEAN      NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL,
    version          BIGINT       NOT NULL,
    CONSTRAINT ck_occurrences_status CHECK (status IN ('PENDING', 'COMPLETED', 'MISSED')),
    CONSTRAINT ck_occurrences_kind CHECK (kind IN ('MANDATORY', 'EXTRA')),
    CONSTRAINT ck_occurrences_completion CHECK ((status = 'COMPLETED') = (completed_at IS NOT NULL))
);

-- Uma missão aparece no máximo uma vez por dia
CREATE UNIQUE INDEX uq_occurrences_task_date ON task_occurrences (task_id, occurrence_date) WHERE task_id IS NOT NULL;
CREATE INDEX ix_occurrences_user_date ON task_occurrences (user_id, occurrence_date);
CREATE INDEX ix_occurrences_plan ON task_occurrences (plan_id);
