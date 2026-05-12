package com.jch.backendapi.user.infrastructure;

import com.jch.backendapi.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserJpaEntityMapper {

    public User toDomain(UserJpaEntity entity) {
        return new User(
                entity.id(),
                entity.email(),
                entity.passwordHash(),
                entity.nickname(),
                entity.role(),
                entity.status(),
                entity.createdAt(),
                entity.updatedAt()
        );
    }

    public UserJpaEntity toJpaEntity(User user) {
        return new UserJpaEntity(
                user.id(),
                user.email(),
                user.passwordHash(),
                user.nickname(),
                user.role(),
                user.status(),
                user.createdAt(),
                user.updatedAt()
        );
    }
}
