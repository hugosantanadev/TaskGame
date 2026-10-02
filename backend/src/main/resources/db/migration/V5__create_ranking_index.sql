-- Ranking semanal (Fase 4): soma as tarefas concluídas de todos os usuários num intervalo de datas.
-- O índice parcial cobre só as concluídas, que são as únicas que pontuam.
CREATE INDEX ix_occurrences_completed_date
    ON task_occurrences (occurrence_date, user_id) WHERE status = 'COMPLETED';
