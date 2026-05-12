package com.jch.backendapi.token.infrastructure;

import com.jch.backendapi.token.domain.RefreshToken;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenJpaEntityMapper {

    public RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return new RefreshToken(
                entity.id(),
                entity.userId(),
                entity.refreshTokenHash(),
                entity.expiresAt(),
                entity.isRevoked(),
                entity.createdAt()
        );
    }

    public RefreshTokenJpaEntity toJpaEntity(RefreshToken refreshToken) {
        return new RefreshTokenJpaEntity(
                refreshToken.id(),
                refreshToken.userId(),
                refreshToken.refreshTokenHash(),
                refreshToken.expiresAt(),
                refreshToken.isRevoked(),
                refreshToken.createdAt()
        );
    }
}
