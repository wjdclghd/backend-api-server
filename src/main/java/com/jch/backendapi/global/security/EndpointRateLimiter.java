package com.jch.backendapi.global.security;

import com.jch.backendapi.global.config.RateLimitProperties;
import com.jch.backendapi.global.error.BusinessException;
import com.jch.backendapi.global.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;

@Component
public class EndpointRateLimiter {

    private final InMemoryRateLimiter rateLimiter;
    private final RequestClientIpExtractor requestClientIpExtractor;
    private final RateLimitProperties rateLimitProperties;

    public EndpointRateLimiter(
            InMemoryRateLimiter rateLimiter,
            RequestClientIpExtractor requestClientIpExtractor,
            RateLimitProperties rateLimitProperties
    ) {
        this.rateLimiter = rateLimiter;
        this.requestClientIpExtractor = requestClientIpExtractor;
        this.rateLimitProperties = rateLimitProperties;
    }

    public void checkSignup(HttpServletRequest request, String email) {
        RateLimitProperties.EndpointPolicy policy = rateLimitProperties.signup();
        String clientIp = requestClientIpExtractor.extract(request);
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        check("signup:ip:" + clientIp, policy.ipCapacity(), policy.ipWindowSeconds());
        check("signup:email:" + normalizedEmail, policy.keyCapacity(), policy.keyWindowSeconds());
    }

    public void checkLogin(HttpServletRequest request, String email) {
        RateLimitProperties.EndpointPolicy policy = rateLimitProperties.login();
        String clientIp = requestClientIpExtractor.extract(request);
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        check("login:ip:" + clientIp, policy.ipCapacity(), policy.ipWindowSeconds());
        check("login:email:" + normalizedEmail, policy.keyCapacity(), policy.keyWindowSeconds());
    }

    public void checkRefresh(HttpServletRequest request, String refreshToken) {
        RateLimitProperties.EndpointPolicy policy = rateLimitProperties.refresh();
        String clientIp = requestClientIpExtractor.extract(request);

        check("refresh:ip:" + clientIp, policy.ipCapacity(), policy.ipWindowSeconds());
        check("refresh:token:" + stableHash(refreshToken), policy.keyCapacity(), policy.keyWindowSeconds());
    }

    public void checkLogout(HttpServletRequest request, String refreshToken) {
        RateLimitProperties.EndpointPolicy policy = rateLimitProperties.logout();
        String clientIp = requestClientIpExtractor.extract(request);

        check("logout:ip:" + clientIp, policy.ipCapacity(), policy.ipWindowSeconds());
        check("logout:token:" + stableHash(refreshToken), policy.keyCapacity(), policy.keyWindowSeconds());
    }

    private void check(String key, int capacity, long windowSeconds) {
        boolean isAllowed = rateLimiter.tryAcquire(key, capacity, Duration.ofSeconds(windowSeconds));
        if (!isAllowed) {
            throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }
    }

    private String stableHash(String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }
}
