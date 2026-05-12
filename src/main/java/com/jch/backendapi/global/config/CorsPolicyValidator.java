package com.jch.backendapi.global.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CorsPolicyValidator implements ApplicationRunner {

    private final AppCorsProperties appCorsProperties;
    private final Environment environment;

    public CorsPolicyValidator(AppCorsProperties appCorsProperties, Environment environment) {
        this.appCorsProperties = appCorsProperties;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean isProdProfile = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (!isProdProfile) {
            return;
        }

        for (String allowedOrigin : appCorsProperties.allowedOrigins()) {
            if (isUnsafeProductionOrigin(allowedOrigin)) {
                throw new IllegalStateException("CORS_ALLOWED_ORIGINS must contain only explicit HTTPS production origins.");
            }
        }
    }

    private boolean isUnsafeProductionOrigin(String allowedOrigin) {
        String normalizedOrigin = allowedOrigin.trim().toLowerCase();
        return normalizedOrigin.equals("*")
                || normalizedOrigin.startsWith("http://")
                || normalizedOrigin.contains("localhost")
                || normalizedOrigin.contains("127.0.0.1")
                || normalizedOrigin.contains("192.168.")
                || normalizedOrigin.contains("10.")
                || normalizedOrigin.contains("172.16.");
    }
}
