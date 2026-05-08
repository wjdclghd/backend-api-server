package com.jch.backendapi.auth.application;

import com.jch.backendapi.auth.dto.LoginRequest;
import com.jch.backendapi.auth.dto.LoginResponse;
import com.jch.backendapi.global.error.AuthException;
import com.jch.backendapi.global.error.ErrorCode;
import com.jch.backendapi.global.security.PasswordHasher;
import com.jch.backendapi.token.domain.AuthToken;
import com.jch.backendapi.token.domain.RefreshToken;
import com.jch.backendapi.token.domain.TokenAuthentication;
import com.jch.backendapi.token.port.RefreshTokenHasher;
import com.jch.backendapi.token.port.RefreshTokenRepository;
import com.jch.backendapi.token.port.TokenProvider;
import com.jch.backendapi.user.domain.User;
import com.jch.backendapi.user.domain.UserRole;
import com.jch.backendapi.user.domain.UserStatus;
import com.jch.backendapi.user.port.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginUseCaseTest {

    @Test
    void executeReturnsTokensWhenCredentialsAreValid() {
        FakeUserRepository userRepository = new FakeUserRepository(activeUser());
        StubPasswordHasher passwordHasher = new StubPasswordHasher();
        SpyRefreshTokenRepository refreshTokenRepository = new SpyRefreshTokenRepository();
        LoginUseCase loginUseCase = new LoginUseCase(
                userRepository,
                passwordHasher,
                new StubTokenProvider(),
                new StubRefreshTokenHasher(),
                refreshTokenRepository
        );
        LoginRequest request = new LoginRequest(" TEST@Example.com ", "password123");

        LoginResponse response = loginUseCase.execute(request);

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("test@example.com", userRepository.requestedEmail);
        assertEquals("hashed-refresh-token", refreshTokenRepository.savedRefreshToken.refreshTokenHash());
        assertNotEquals("refresh-token", refreshTokenRepository.savedRefreshToken.refreshTokenHash());
    }

    @Test
    void executeThrowsInvalidCredentialsWhenUserDoesNotExist() {
        LoginUseCase loginUseCase = new LoginUseCase(
                new FakeUserRepository(null),
                new StubPasswordHasher(),
                new StubTokenProvider(),
                new StubRefreshTokenHasher(),
                new SpyRefreshTokenRepository()
        );
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        AuthException exception = assertThrows(AuthException.class, () -> loginUseCase.execute(request));

        assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.errorCode());
    }

    @Test
    void executeThrowsInvalidCredentialsWhenPasswordDoesNotMatch() {
        LoginUseCase loginUseCase = new LoginUseCase(
                new FakeUserRepository(activeUser()),
                new StubPasswordHasher(),
                new StubTokenProvider(),
                new StubRefreshTokenHasher(),
                new SpyRefreshTokenRepository()
        );
        LoginRequest request = new LoginRequest("test@example.com", "wrong-password");

        AuthException exception = assertThrows(AuthException.class, () -> loginUseCase.execute(request));

        assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.errorCode());
    }

    @Test
    void executeThrowsInactiveUserWhenUserIsInactive() {
        LoginUseCase loginUseCase = new LoginUseCase(
                new FakeUserRepository(inactiveUser()),
                new StubPasswordHasher(),
                new StubTokenProvider(),
                new StubRefreshTokenHasher(),
                new SpyRefreshTokenRepository()
        );
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        AuthException exception = assertThrows(AuthException.class, () -> loginUseCase.execute(request));

        assertEquals(ErrorCode.INACTIVE_USER, exception.errorCode());
    }

    private static User activeUser() {
        Instant now = Instant.now();
        return new User(
                1L,
                "test@example.com",
                "hashed-password123",
                "jch",
                UserRole.USER,
                UserStatus.ACTIVE,
                now,
                now
        );
    }

    private static User inactiveUser() {
        Instant now = Instant.now();
        return new User(
                1L,
                "test@example.com",
                "hashed-password123",
                "jch",
                UserRole.USER,
                UserStatus.INACTIVE,
                now,
                now
        );
    }

    private static class FakeUserRepository implements UserRepository {

        private final User user;
        private String requestedEmail;

        private FakeUserRepository(User user) {
            this.user = user;
        }

        @Override
        public User save(User user) {
            return user;
        }

        @Override
        public boolean existsByEmail(String email) {
            return user != null;
        }

        @Override
        public Optional<User> findById(Long id) {
            return Optional.ofNullable(user);
        }

        @Override
        public Optional<User> findByEmail(String email) {
            requestedEmail = email;
            return Optional.ofNullable(user);
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

        @Override
        public Optional<TokenAuthentication> authenticateAccessToken(String accessToken) {
            return Optional.empty();
        }
    }

    private static class StubRefreshTokenHasher implements RefreshTokenHasher {

        @Override
        public String hash(String refreshToken) {
            return "hashed-" + refreshToken;
        }
    }

    private static class SpyRefreshTokenRepository implements RefreshTokenRepository {

        private RefreshToken savedRefreshToken;

        @Override
        public RefreshToken save(RefreshToken refreshToken) {
            savedRefreshToken = refreshToken;
            return refreshToken;
        }

        @Override
        public Optional<RefreshToken> findByRefreshTokenHash(String refreshTokenHash) {
            return Optional.ofNullable(savedRefreshToken);
        }

        @Override
        public long revokeActiveByUserId(Long userId) {
            return 0;
        }

        @Override
        public long deleteExpiredOrRevoked(Instant now) {
            return 0;
        }
    }
}
