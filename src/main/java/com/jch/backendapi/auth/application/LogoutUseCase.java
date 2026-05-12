package com.jch.backendapi.auth.application;

import com.jch.backendapi.auth.dto.RefreshTokenRequest;
import com.jch.backendapi.token.port.RefreshTokenHasher;
import com.jch.backendapi.token.port.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutUseCase {

    private final RefreshTokenHasher refreshTokenHasher;
    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutUseCase(RefreshTokenHasher refreshTokenHasher, RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenHasher = refreshTokenHasher;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public void execute(RefreshTokenRequest request) {
        String refreshTokenHash = refreshTokenHasher.hash(request.refreshToken());
        refreshTokenRepository.findByRefreshTokenHash(refreshTokenHash)
                .filter(refreshToken -> !refreshToken.isRevoked())
                .ifPresent(refreshToken -> refreshTokenRepository.save(refreshToken.revoke()));
    }
}
