package com.jch.backendapi.auth.presentation;

import com.jch.backendapi.global.security.PasswordHasher;
import com.jch.backendapi.user.infrastructure.UserJpaEntity;
import com.jch.backendapi.user.infrastructure.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSignupIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Test
    void signupCreatesUserThroughSpringContext() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "integration@example.com",
                                  "password": "password123",
                                  "nickname": "jch"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("integration@example.com")))
                .andExpect(jsonPath("$.nickname", is("jch")))
                .andExpect(jsonPath("$.role", is("USER")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        UserJpaEntity user = userJpaRepository.findByEmail("integration@example.com").orElseThrow();

        assertNotEquals("password123", user.passwordHash());
        assertTrue(passwordHasher.matches("password123", user.passwordHash()));
    }

    @Test
    void signupReturnsConflictWhenDatabaseEmailConstraintIsViolated() throws Exception {
        String body = """
                {
                  "email": "duplicate-integration@example.com",
                  "password": "password123",
                  "nickname": "jch"
                }
                """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("AUTH_DUPLICATE_EMAIL")));
    }
}
