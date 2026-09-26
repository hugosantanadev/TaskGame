package com.gasmtask.auth.domain;

public enum RevocationReason {
    /** Trocado por um token novo numa renovação normal. */
    ROTATED,
    /** Sessão encerrada pelo usuário. */
    LOGOUT,
    /** Um token já rotacionado foi reapresentado: sinal de roubo, a sessão inteira é revogada. */
    REUSE_DETECTED
}
