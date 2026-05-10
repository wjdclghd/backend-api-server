package com.jch.backendapi.auth.application;

import com.jch.backendapi.auth.dto.AuthUserResponse;
import com.jch.backendapi.auth.dto.LoginResponse;
import com.jch.backendapi.auth.dto.RefreshTokenRequest;
import com.jch.backendapi.auth.dto.TokenResponse;
import com.jch.backendapi.global.error.AuthException;
import com.jch.backendapi.global.error.ErrorCode;
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

import java.time.Instant;

@Service
public class RefreshTokenUseCase {

    private final RefreshTokenHasher refreshTokenHasher;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    public RefreshTokenUseCase(
            RefreshTokenHasher refreshTokenHasher,
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            TokenProvider tokenProvider
    ) {
        this.refreshTokenHasher = refreshTokenHasher;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    @Transactional(noRollbackFor = AuthException.class)
    public LoginResponse execute(RefreshTokenRequest request) {
        String refreshTokenHash = refreshTokenHasher.hash(request.refreshToken());
        RefreshToken currentRefreshToken = refreshTokenRepository.findByRefreshTokenHash(refreshTokenHash)
                .orElseThrow(() -> new AuthException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (currentRefreshToken.isRevoked()) {
            refreshTokenRepository.revokeActiveByUserId(currentRefreshToken.userId());
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (currentRefreshToken.isExpired(Instant.now())) {
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findById(currentRefreshToken.userId())
                .orElseThrow(() -> new AuthException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (user.status() != UserStatus.ACTIVE) {
            throw new AuthException(ErrorCode.INACTIVE_USER);
        }

        refreshTokenRepository.save(currentRefreshToken.revoke());

        AuthToken accessToken = tokenProvider.issueAccessToken(user);
        AuthToken refreshToken = tokenProvider.issueRefreshToken(user);
        String newRefreshTokenHash = refreshTokenHasher.hash(refreshToken.value());
        refreshTokenRepository.save(RefreshToken.issue(user.id(), newRefreshTokenHash, refreshToken.expiresAt()));

        return new LoginResponse(
                new TokenResponse(
                        accessToken.value(),
                        refreshToken.value(),
                        accessToken.expiresAt(),
                        refreshToken.expiresAt()
                ),
                new AuthUserResponse(
                        user.id(),
                        user.email(),
                        user.nickname(),
                        user.role(),
                        user.status()
                )
        );
    }
}
