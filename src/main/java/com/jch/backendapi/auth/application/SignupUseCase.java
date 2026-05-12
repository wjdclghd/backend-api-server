package com.jch.backendapi.auth.application;

import com.jch.backendapi.auth.dto.SignupRequest;
import com.jch.backendapi.auth.dto.SignupResponse;
import com.jch.backendapi.global.error.AuthException;
import com.jch.backendapi.global.error.ErrorCode;
import com.jch.backendapi.global.security.PasswordHasher;
import com.jch.backendapi.user.domain.User;
import com.jch.backendapi.user.port.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class SignupUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public SignupUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public SignupResponse execute(SignupRequest request) {
        String email = normalizeEmail(request.email());
        String rawPassword = request.password();
        String nickname = request.nickname().trim();

        if (userRepository.existsByEmail(email)) {
            throw new AuthException(ErrorCode.DUPLICATE_EMAIL);
        }

        String passwordHash = passwordHasher.hash(rawPassword);
        User user = User.signup(email, passwordHash, nickname);
        User savedUser = userRepository.save(user);

        return new SignupResponse(
                savedUser.id(),
                savedUser.email(),
                savedUser.nickname(),
                savedUser.role(),
                savedUser.status(),
                savedUser.createdAt()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
