package com.gasmtask.shared.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Relógio único da aplicação. Toda regra que depende de "agora" recebe este {@link Clock}
 * em vez de chamar {@code Instant.now()}, o que permite testar regras de horário de forma determinística.
 */
@Configuration(proxyBeanMethods = false)
public class ClockConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
