package com.jch.backendapi.auth.dto;

import java.time.Instant;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt
) {
}
