package com.jch.backendapi.token.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, Long> {

    Optional<RefreshTokenJpaEntity> findByRefreshTokenHash(String refreshTokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
                    update refresh_tokens
                    set is_revoked = true
                    where user_id = :userId
                      and is_revoked = false
                    """,
            nativeQuery = true
    )
    long revokeActiveByUserId(@Param("userId") Long userId);

    long deleteByExpiresAtBeforeOrIsRevokedTrue(Instant now);
}
