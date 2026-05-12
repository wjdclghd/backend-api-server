package com.jch.backendapi.user.infrastructure;

import com.jch.backendapi.user.domain.User;
import com.jch.backendapi.user.port.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserJpaEntityMapper userJpaEntityMapper;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository, UserJpaEntityMapper userJpaEntityMapper) {
        this.userJpaRepository = userJpaRepository;
        this.userJpaEntityMapper = userJpaEntityMapper;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = userJpaEntityMapper.toJpaEntity(user);
        UserJpaEntity savedEntity = userJpaRepository.save(entity);
        return userJpaEntityMapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id)
                .map(userJpaEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(userJpaEntityMapper::toDomain);
    }
}
