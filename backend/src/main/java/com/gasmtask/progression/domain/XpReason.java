package com.gasmtask.progression.domain;

/** Por que o XP mudou. BACKFILL é o saldo inicial calculado do histórico de quem já usava o app. */
public enum XpReason {
    TASK_COMPLETED, DAY_FULFILLED, TASK_MISSED, BACKFILL
}
