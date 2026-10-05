package com.gasmtask.user.config;

import com.gasmtask.user.domain.Plan;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Limites de cada plano, para o app virar SaaS sem espalhar regra de cobrança pelo código. Zero é "sem limite":
 * hoje os dois planos são ilimitados; para cobrar, basta pôr números aqui.
 */
@ConfigurationProperties("app.plans")
public record PlanProperties(Limits free, Limits pro) {

    public PlanProperties {
        free = free == null ? Limits.UNLIMITED : free;
        pro = pro == null ? Limits.UNLIMITED : pro;
    }

    public Limits of(Plan plan) {
        return plan == Plan.PRO ? pro : free;
    }

    /** @param maxActiveMissions missões ativas ao mesmo tempo; 0 é sem limite */
    public record Limits(int maxActiveMissions) {

        static final Limits UNLIMITED = new Limits(0);

        public boolean allowsMoreMissions(long active) {
            return maxActiveMissions <= 0 || active < maxActiveMissions;
        }
    }
}
