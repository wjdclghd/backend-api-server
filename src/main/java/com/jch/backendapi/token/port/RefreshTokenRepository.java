package com.jch.backendapi.token.port;

import com.jch.backendapi.token.domain.RefreshToken;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByRefreshTokenHash(String refreshTokenHash);

    long revokeActiveByUserId(Long userId);

    long deleteExpiredOrRevoked(Instant now);
}
