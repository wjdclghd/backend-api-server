package com.jch.backendapi.auth.dto;

import com.jch.backendapi.user.domain.UserRole;
import com.jch.backendapi.user.domain.UserStatus;

import java.time.Instant;

public record SignupResponse(
        Long userId,
        String email,
        String nickname,
        UserRole role,
        UserStatus status,
        Instant createdAt
) {
}
