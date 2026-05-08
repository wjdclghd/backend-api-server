package com.jch.backendapi.auth.presentation;

import com.jch.backendapi.token.infrastructure.RefreshTokenJpaEntity;
import com.jch.backendapi.token.infrastructure.RefreshTokenJpaRepository;
import com.jch.backendapi.token.port.RefreshTokenHasher;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthLoginIntegrationTest {

    private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile("\"accessToken\":\"([^\"]+)\"");
    private static final Pattern REFRESH_TOKEN_PATTERN = Pattern.compile("\"refreshToken\":\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Autowired
    private RefreshTokenHasher refreshTokenHasher;

    @Autowired
    private EntityManager entityManager;

    @Test
    void loginReturnsTokensAfterSignup() throws Exception {
        signup("login-integration@example.com", "password123", "jch");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "login-integration@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.refreshToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.accessTokenExpiresAt", not(blankOrNullString())))
                .andExpect(jsonPath("$.refreshTokenExpiresAt", not(blankOrNullString())))
                .andReturn();

        String refreshToken = extractRefreshToken(result.getResponse().getContentAsString());
        RefreshTokenJpaEntity savedRefreshToken = refreshTokenJpaRepository
                .findByRefreshTokenHash(refreshTokenHasher.hash(refreshToken))
                .orElseThrow();

        assertNotEquals(refreshToken, savedRefreshToken.refreshTokenHash());
        assertTrue(refreshTokenHasher.hash(refreshToken).equals(savedRefreshToken.refreshTokenHash()));
    }

    @Test
    void refreshRotatesRefreshTokenAndRevokesOldToken() throws Exception {
        signup("refresh-integration@example.com", "password123", "jch");
        MvcResult loginResult = login("refresh-integration@example.com", "password123");
        String oldAccessToken = extractAccessToken(loginResult.getResponse().getContentAsString());
        String oldRefreshToken = extractRefreshToken(loginResult.getResponse().getContentAsString());

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(oldRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.refreshToken", not(blankOrNullString())))
                .andReturn();

        String newAccessToken = extractAccessToken(refreshResult.getResponse().getContentAsString());
        String newRefreshToken = extractRefreshToken(refreshResult.getResponse().getContentAsString());

        RefreshTokenJpaEntity oldSavedRefreshToken = refreshTokenJpaRepository
                .findByRefreshTokenHash(refreshTokenHasher.hash(oldRefreshToken))
                .orElseThrow();
        RefreshTokenJpaEntity newSavedRefreshToken = refreshTokenJpaRepository
                .findByRefreshTokenHash(refreshTokenHasher.hash(newRefreshToken))
                .orElseThrow();

        assertTrue(oldSavedRefreshToken.isRevoked());
        assertNotEquals(oldAccessToken, newAccessToken);
        assertNotEquals(oldRefreshToken, newRefreshToken);
        assertTrue(!newSavedRefreshToken.isRevoked());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(oldRefreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_INVALID_REFRESH_TOKEN"));

        entityManager.clear();
        RefreshTokenJpaEntity revokedNewSavedRefreshToken = refreshTokenJpaRepository
                .findByRefreshTokenHash(refreshTokenHasher.hash(newRefreshToken))
                .orElseThrow();

        assertTrue(revokedNewSavedRefreshToken.isRevoked());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(newRefreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_INVALID_REFRESH_TOKEN"));
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        signup("logout-integration@example.com", "password123", "jch");
        MvcResult loginResult = login("logout-integration@example.com", "password123");
        String refreshToken = extractRefreshToken(loginResult.getResponse().getContentAsString());

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isNoContent());

        RefreshTokenJpaEntity savedRefreshToken = refreshTokenJpaRepository
                .findByRefreshTokenHash(refreshTokenHasher.hash(refreshToken))
                .orElseThrow();

        assertTrue(savedRefreshToken.isRevoked());
    }

    @Test
    void loginReturnsUnauthorizedWhenPasswordDoesNotMatch() throws Exception {
        signup("invalid-password@example.com", "password123", "jch");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-password@example.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    void loginReturnsUnauthorizedWhenUserDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "missing-user@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_INVALID_CREDENTIALS"));
    }

    private void signup(String email, String password, String nickname) throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s",
                                  "nickname": "%s"
                                }
                                """.formatted(email, password, nickname)))
                .andExpect(status().isCreated());
    }

    private MvcResult login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String extractAccessToken(String responseBody) {
        Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(responseBody);
        if (!matcher.find()) {
            throw new IllegalStateException("accessToken not found in response");
        }
        return matcher.group(1);
    }

    private String extractRefreshToken(String responseBody) {
        Matcher matcher = REFRESH_TOKEN_PATTERN.matcher(responseBody);
        if (!matcher.find()) {
            throw new IllegalStateException("refreshToken not found in response");
        }
        return matcher.group(1);
    }
}
