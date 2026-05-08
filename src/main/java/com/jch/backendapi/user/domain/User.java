package com.jch.backendapi.user.domain;

import java.time.Instant;
import java.util.Objects;

public class User {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String nickname;
    private final UserRole role;
    private final UserStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    public User(
            Long id,
            String email,
            String passwordHash,
            String nickname,
            UserRole role,
            UserStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.nickname = Objects.requireNonNull(nickname);
        this.role = Objects.requireNonNull(role);
        this.status = Objects.requireNonNull(status);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User signup(String email, String passwordHash, String nickname) {
        Instant now = Instant.now();
        return new User(
                null,
                email,
                passwordHash,
                nickname,
                UserRole.USER,
                UserStatus.ACTIVE,
                now,
                now
        );
    }

    public Long id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public String nickname() {
        return nickname;
    }

    public UserRole role() {
        return role;
    }

    public UserStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
