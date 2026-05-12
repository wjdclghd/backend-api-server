package com.jch.backendapi.token.domain;

import com.jch.backendapi.user.domain.UserRole;

public record TokenAuthentication(
        Long userId,
        String email,
        UserRole role
) {
}
