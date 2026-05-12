package com.jch.backendapi.user.infrastructure;

import com.jch.backendapi.user.domain.User;
import com.jch.backendapi.user.domain.UserRole;
import com.jch.backendapi.user.domain.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserJpaEntityMapperTest {

    @Test
    void mapsBetweenDomainAndJpaEntity() {
        Instant now = Instant.now();
        User user = new User(
                1L,
                "test@example.com",
                "passwordHash",
                "jch",
                UserRole.USER,
                UserStatus.ACTIVE,
                now,
                now
        );
        UserJpaEntityMapper mapper = new UserJpaEntityMapper();

        UserJpaEntity entity = mapper.toJpaEntity(user);
        User mappedUser = mapper.toDomain(entity);

        assertEquals(user.id(), mappedUser.id());
        assertEquals(user.email(), mappedUser.email());
        assertEquals(user.passwordHash(), mappedUser.passwordHash());
        assertEquals(user.nickname(), mappedUser.nickname());
        assertEquals(user.role(), mappedUser.role());
        assertEquals(user.status(), mappedUser.status());
        assertEquals(user.createdAt(), mappedUser.createdAt());
        assertEquals(user.updatedAt(), mappedUser.updatedAt());
    }
}
