package com.jch.backendapi.token.infrastructure;

import com.jch.backendapi.token.domain.RefreshToken;
import com.jch.backendapi.token.port.RefreshTokenRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;
    private final RefreshTokenJpaEntityMapper refreshTokenJpaEntityMapper;

    public RefreshTokenRepositoryAdapter(
            RefreshTokenJpaRepository refreshTokenJpaRepository,
            RefreshTokenJpaEntityMapper refreshTokenJpaEntityMapper
    ) {
        this.refreshTokenJpaRepository = refreshTokenJpaRepository;
        this.refreshTokenJpaEntityMapper = refreshTokenJpaEntityMapper;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity entity = refreshTokenJpaEntityMapper.toJpaEntity(refreshToken);
        RefreshTokenJpaEntity savedEntity = refreshTokenJpaRepository.save(entity);
        return refreshTokenJpaEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<RefreshToken> findByRefreshTokenHash(String refreshTokenHash) {
        return refreshTokenJpaRepository.findByRefreshTokenHash(refreshTokenHash)
                .map(refreshTokenJpaEntityMapper::toDomain);
    }

    @Override
    public long revokeActiveByUserId(Long userId) {
        return refreshTokenJpaRepository.revokeActiveByUserId(userId);
    }

    @Override
    public long deleteExpiredOrRevoked(Instant now) {
        return refreshTokenJpaRepository.deleteByExpiresAtBeforeOrIsRevokedTrue(now);
    }
}
