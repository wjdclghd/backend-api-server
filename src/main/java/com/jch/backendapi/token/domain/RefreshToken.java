package com.jch.backendapi.token.domain;

import java.time.Instant;
import java.util.Objects;

public class RefreshToken {

    private final Long id;
    private final Long userId;
    private final String refreshTokenHash;
    private final Instant expiresAt;
    private final boolean isRevoked;
    private final Instant createdAt;

    public RefreshToken(
            Long id,
            Long userId,
            String refreshTokenHash,
            Instant expiresAt,
            boolean isRevoked,
            Instant createdAt
    ) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId);
        this.refreshTokenHash = Objects.requireNonNull(refreshTokenHash);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.isRevoked = isRevoked;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static RefreshToken issue(Long userId, String refreshTokenHash, Instant expiresAt) {
        return new RefreshToken(null, userId, refreshTokenHash, expiresAt, false, Instant.now());
    }

    public RefreshToken revoke() {
        return new RefreshToken(id, userId, refreshTokenHash, expiresAt, true, createdAt);
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public Long id() {
        return id;
    }

    public Long userId() {
        return userId;
    }

    public String refreshTokenHash() {
        return refreshTokenHash;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return isRevoked;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
