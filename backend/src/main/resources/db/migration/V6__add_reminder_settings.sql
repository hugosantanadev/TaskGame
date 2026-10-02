-- Preferências de lembrete (Fase 4), embutidas no usuário: avisar das tarefas e com quanta antecedência,
-- e os horários de dormir e de acordar (opcionais). Contas existentes ficam com os padrões.
ALTER TABLE users
    ADD COLUMN reminder_tasks_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN reminder_lead_minutes  INTEGER NOT NULL DEFAULT 10,
    ADD COLUMN bedtime                TIME,
    ADD COLUMN wake_time              TIME,
    ADD CONSTRAINT ck_users_reminder_lead_minutes CHECK (reminder_lead_minutes BETWEEN 0 AND 120);
