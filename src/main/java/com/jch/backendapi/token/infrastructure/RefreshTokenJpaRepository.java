package com.jch.backendapi.token.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, Long> {

    Optional<RefreshTokenJpaEntity> findByRefreshTokenHash(String refreshTokenHash);

    long deleteByExpiresAtBeforeOrIsRevokedTrue(Instant now);
}
