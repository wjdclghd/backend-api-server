package com.jch.backendapi.auth.application;

import com.jch.backendapi.auth.dto.LoginRequest;
import com.jch.backendapi.auth.dto.LoginResponse;
import com.jch.backendapi.global.error.AuthException;
import com.jch.backendapi.global.error.ErrorCode;
import com.jch.backendapi.global.security.PasswordHasher;
import com.jch.backendapi.token.domain.AuthToken;
import com.jch.backendapi.token.domain.RefreshToken;
import com.jch.backendapi.token.port.RefreshTokenHasher;
import com.jch.backendapi.token.port.RefreshTokenRepository;
import com.jch.backendapi.token.port.TokenProvider;
import com.jch.backendapi.user.domain.User;
import com.jch.backendapi.user.domain.UserStatus;
import com.jch.backendapi.user.port.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final RefreshTokenHasher refreshTokenHasher;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenProvider tokenProvider,
            RefreshTokenHasher refreshTokenHasher,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.refreshTokenHasher = refreshTokenHasher;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public LoginResponse execute(LoginRequest request) {
        String email = normalizeEmail(request.email());
        String rawPassword = request.password();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordHasher.matches(rawPassword, user.passwordHash())) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (user.status() != UserStatus.ACTIVE) {
            throw new AuthException(ErrorCode.INACTIVE_USER);
        }

        AuthToken accessToken = tokenProvider.issueAccessToken(user);
        AuthToken refreshToken = tokenProvider.issueRefreshToken(user);
        String refreshTokenHash = refreshTokenHasher.hash(refreshToken.value());

        refreshTokenRepository.save(RefreshToken.issue(user.id(), refreshTokenHash, refreshToken.expiresAt()));

        return new LoginResponse(
                accessToken.value(),
                refreshToken.value(),
                accessToken.expiresAt(),
                refreshToken.expiresAt()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
