package com.jch.backendapi.token.infrastructure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @NotBlank
        String issuer,

        @NotBlank
        @Size(min = 32)
        String secret,

        @Positive
        long accessTokenExpirationSeconds,

        @Positive
        long refreshTokenExpirationSeconds
) {
    public static final String LOCAL_DEVELOPMENT_SECRET = "local-development-jwt-secret-must-be-changed-32bytes";
}
