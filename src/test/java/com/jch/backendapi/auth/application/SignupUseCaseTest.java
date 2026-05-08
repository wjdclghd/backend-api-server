package com.jch.backendapi.auth.application;

import com.jch.backendapi.auth.dto.SignupRequest;
import com.jch.backendapi.auth.dto.SignupResponse;
import com.jch.backendapi.global.error.AuthException;
import com.jch.backendapi.global.error.ErrorCode;
import com.jch.backendapi.global.security.PasswordHasher;
import com.jch.backendapi.user.domain.User;
import com.jch.backendapi.user.domain.UserRole;
import com.jch.backendapi.user.domain.UserStatus;
import com.jch.backendapi.user.port.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SignupUseCaseTest {

    @Test
    void executeCreatesUserWithHashedPassword() {
        FakeUserRepository userRepository = new FakeUserRepository();
        StubPasswordHasher passwordHasher = new StubPasswordHasher();
        SignupUseCase signupUseCase = new SignupUseCase(userRepository, passwordHasher);
        SignupRequest request = new SignupRequest(" TEST@Example.com ", "password123", " jch ");

        SignupResponse response = signupUseCase.execute(request);

        assertEquals(1L, response.userId());
        assertEquals("test@example.com", response.email());
        assertEquals("jch", response.nickname());
        assertEquals(UserRole.USER, response.role());
        assertEquals(UserStatus.ACTIVE, response.status());
        assertEquals("test@example.com", userRepository.savedUser.email());
        assertEquals("hashed-password123", userRepository.savedUser.passwordHash());
        assertNotEquals("password123", userRepository.savedUser.passwordHash());
    }

    @Test
    void executeThrowsAuthExceptionWhenEmailAlreadyExists() {
        FakeUserRepository userRepository = new FakeUserRepository();
        userRepository.isEmailExists = true;
        SignupUseCase signupUseCase = new SignupUseCase(userRepository, new StubPasswordHasher());
        SignupRequest request = new SignupRequest("test@example.com", "password123", "jch");

        AuthException exception = assertThrows(AuthException.class, () -> signupUseCase.execute(request));

        assertEquals(ErrorCode.DUPLICATE_EMAIL, exception.errorCode());
    }

    private static class FakeUserRepository implements UserRepository {

        private boolean isEmailExists;
        private User savedUser;

        @Override
        public User save(User user) {
            Instant now = Instant.now();
            savedUser = new User(
                    1L,
                    user.email(),
                    user.passwordHash(),
                    user.nickname(),
                    user.role(),
                    user.status(),
                    now,
                    now
            );
            return savedUser;
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
