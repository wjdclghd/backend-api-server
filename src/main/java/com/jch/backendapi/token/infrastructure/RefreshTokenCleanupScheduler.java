package com.jch.backendapi.token.infrastructure;

import com.jch.backendapi.token.port.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCleanupScheduler(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(cron = "${refresh-token.cleanup-cron:0 0 3 * * *}")
    @Transactional
    public void deleteExpiredOrRevokedRefreshTokens() {
        refreshTokenRepository.deleteExpiredOrRevoked(Instant.now());
    }
}
