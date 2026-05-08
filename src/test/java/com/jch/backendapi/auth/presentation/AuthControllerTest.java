package com.jch.backendapi.auth.presentation;

import com.jch.backendapi.auth.application.LoginUseCase;
import com.jch.backendapi.auth.application.LogoutUseCase;
import com.jch.backendapi.auth.application.RefreshTokenUseCase;
import com.jch.backendapi.auth.application.SignupUseCase;
import com.jch.backendapi.global.error.GlobalExceptionHandler;
import com.jch.backendapi.global.security.PasswordHasher;
import com.jch.backendapi.token.domain.AuthToken;
import com.jch.backendapi.token.domain.RefreshToken;
import com.jch.backendapi.token.port.RefreshTokenHasher;
import com.jch.backendapi.token.port.RefreshTokenRepository;
import com.jch.backendapi.token.port.TokenProvider;
import com.jch.backendapi.user.domain.User;
import com.jch.backendapi.user.port.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Optional;

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
        LoginUseCase loginUseCase = new LoginUseCase(
                userRepository,
                passwordHasher,
                new StubTokenProvider(),
                new StubRefreshTokenHasher(),
                new FakeRefreshTokenRepository()
        );
        RefreshTokenUseCase refreshTokenUseCase = new RefreshTokenUseCase(
                new StubRefreshTokenHasher(),
                new FakeRefreshTokenRepository(),
                userRepository,
                new StubTokenProvider()
        );
        LogoutUseCase logoutUseCase = new LogoutUseCase(new StubRefreshTokenHasher(), new FakeRefreshTokenRepository());
        AuthController authController = new AuthController(signupUseCase, loginUseCase, refreshTokenUseCase, logoutUseCase);
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

        @Override
        public Optional<User> findById(Long id) {
            return Optional.empty();
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.empty();
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

    private static class StubTokenProvider implements TokenProvider {

        @Override
        public AuthToken issueAccessToken(User user) {
            return new AuthToken("access-token", Instant.now().plusSeconds(900));
        }

        @Override
        public AuthToken issueRefreshToken(User user) {
            return new AuthToken("refresh-token", Instant.now().plusSeconds(1209600));
        }
    }

    private static class StubRefreshTokenHasher implements RefreshTokenHasher {

        @Override
        public String hash(String refreshToken) {
            return "hashed-" + refreshToken;
        }
    }

    private static class FakeRefreshTokenRepository implements RefreshTokenRepository {

        @Override
        public RefreshToken save(RefreshToken refreshToken) {
            return refreshToken;
        }

        @Override
        public Optional<RefreshToken> findByRefreshTokenHash(String refreshTokenHash) {
            return Optional.empty();
        }

        @Override
        public long deleteExpiredOrRevoked(Instant now) {
            return 0;
        }
    }
}
