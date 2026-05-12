package com.jch.backendapi.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(AppCorsProperties appCorsProperties) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(appCorsProperties.allowedOrigins());
        corsConfiguration.setAllowedMethods(appCorsProperties.allowedMethods());
        corsConfiguration.setAllowedHeaders(appCorsProperties.allowedHeaders());
        corsConfiguration.setAllowCredentials(false);
        corsConfiguration.setMaxAge(appCorsProperties.maxAgeSeconds());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}
