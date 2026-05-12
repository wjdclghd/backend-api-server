package com.jch.backendapi.auth.dto;

public record LoginResponse(
        TokenResponse token,
        AuthUserResponse user
) {
}
