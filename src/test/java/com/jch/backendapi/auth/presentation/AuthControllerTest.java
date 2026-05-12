package com.jch.backendapi.auth.presentation;

import com.jch.backendapi.auth.application.SignupUseCase;
import com.jch.backendapi.global.config.RateLimitProperties;
import com.jch.backendapi.global.error.GlobalExceptionHandler;
import com.jch.backendapi.global.security.EndpointRateLimiter;
import com.jch.backendapi.global.security.InMemoryRateLimiter;
import com.jch.backendapi.global.security.PasswordHasher;
import com.jch.backendapi.global.security.RequestClientIpExtractor;
import com.jch.backendapi.user.domain.User;
import com.jch.backendapi.user.port.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    @Test
    void signupReturnsCreatedResponse() throws Exception {
        MockMvc mockMvc = createMockMvc(new FakeUserRepository());

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test@example.com",
                                  "password": "password123",
                                  "nickname": "jch"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.nickname", is("jch")))
                .andExpect(jsonPath("$.role", is("USER")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void signupReturnsBadRequestWhenRequestIsInvalid() throws Exception {
        MockMvc mockMvc = createMockMvc(new FakeUserRepository());

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email",
                                  "password": "short",
                                  "nickname": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_REQUEST")));
    }

    @Test
    void signupReturnsConflictWhenEmailAlreadyExists() throws Exception {
        FakeUserRepository userRepository = new FakeUserRepository();
        userRepository.isEmailExists = true;
        MockMvc mockMvc = createMockMvc(userRepository);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test@example.com",
                                  "password": "password123",
                                  "nickname": "jch"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("AUTH_DUPLICATE_EMAIL")));
    }

    private MockMvc createMockMvc(FakeUserRepository userRepository) {
        StubPasswordHasher passwordHasher = new StubPasswordHasher();
        SignupUseCase signupUseCase = new SignupUseCase(userRepository, passwordHasher);
        EndpointRateLimiter endpointRateLimiter = new EndpointRateLimiter(
                new InMemoryRateLimiter(),
                new RequestClientIpExtractor(),
                new RateLimitProperties()
        );
        AuthController authController = new AuthController(signupUseCase, endpointRateLimiter);
        return MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static class FakeUserRepository implements UserRepository {

        private boolean isEmailExists;

        @Override
        public User save(User user) {
            Instant now = Instant.now();
            return new User(
                    1L,
                    user.email(),
                    user.passwordHash(),
                    user.nickname(),
                    user.role(),
                    user.status(),
                    now,
                    now
            );
        }

        @Override
        public boolean existsByEmail(String email) {
            return isEmailExists;
        }
    }

    private static class StubPasswordHasher implements PasswordHasher {

        @Override
        public String hash(String rawPassword) {
            return "hashed-" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String passwordHash) {
            return passwordHash.equals(hash(rawPassword));
        }
    }
}
