package com.jch.backendapi.global.security;

import com.jch.backendapi.global.config.RateLimitProperties;
import com.jch.backendapi.global.error.BusinessException;
import com.jch.backendapi.global.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;
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

    private void check(String key, int capacity, long windowSeconds) {
        boolean isAllowed = rateLimiter.tryAcquire(key, capacity, Duration.ofSeconds(windowSeconds));
        if (!isAllowed) {
            throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }
    }
}
