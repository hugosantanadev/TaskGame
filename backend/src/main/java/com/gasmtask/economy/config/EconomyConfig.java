package com.gasmtask.economy.config;

import com.gasmtask.economy.domain.RewardPolicy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class EconomyConfig {

    @Bean
    RewardPolicy rewardPolicy(RewardProperties properties) {
        return properties.toPolicy();
    }
}
