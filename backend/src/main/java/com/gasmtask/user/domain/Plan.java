package com.gasmtask.user.domain;

/**
 * Plano da conta. Todo mundo começa no FREE; o PRO existe para o app virar SaaS (cobrança, limites maiores).
 * Os limites de cada plano ficam na configuração ({@code app.plans}), não espalhados pelo código.
 */
public enum Plan {
    FREE, PRO
}
