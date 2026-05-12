package com.jch.backendapi.token.infrastructure;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class JwtSecretValidator implements ApplicationRunner {

    private final JwtProperties jwtProperties;
    private final Environment environment;

    public JwtSecretValidator(JwtProperties jwtProperties, Environment environment) {
        this.jwtProperties = jwtProperties;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean isProdProfile = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (isProdProfile && JwtProperties.LOCAL_DEVELOPMENT_SECRET.equals(jwtProperties.secret())) {
            throw new IllegalStateException("JWT_SECRET must be configured in prod profile.");
        }
    }
}
