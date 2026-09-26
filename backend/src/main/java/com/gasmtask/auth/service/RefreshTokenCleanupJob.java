package com.gasmtask.auth.service;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Remove do banco tokens expirados há mais de uma semana. */
@Component
public class RefreshTokenCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupJob.class);
    private static final Duration RETENTION = Duration.ofDays(7);

    private final RefreshTokenService refreshTokenService;

    public RefreshTokenCleanupJob(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @Scheduled(cron = "${app.security.refresh-token.cleanup-cron}")
    public void purgeExpiredTokens() {
        int removed = refreshTokenService.purgeExpired(RETENTION);
        if (removed > 0) {
            log.info("{} refresh tokens expirados removidos", removed);
        }
    }
}
