package com.jch.backendapi.token.domain;

import java.time.Instant;
import java.util.Objects;

public class AuthToken {

    private final String value;
    private final Instant expiresAt;

    public AuthToken(String value, Instant expiresAt) {
        this.value = Objects.requireNonNull(value);
        this.expiresAt = Objects.requireNonNull(expiresAt);
    }

    public String value() {
        return value;
    }

    public Instant expiresAt() {
        return expiresAt;
    }
}
