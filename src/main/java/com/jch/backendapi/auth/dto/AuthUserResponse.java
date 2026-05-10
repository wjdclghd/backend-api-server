package com.jch.backendapi.auth.dto;

import com.jch.backendapi.user.domain.UserRole;
import com.jch.backendapi.user.domain.UserStatus;

public record AuthUserResponse(
        Long userId,
        String email,
        String nickname,
        UserRole role,
        UserStatus status
) {
}
