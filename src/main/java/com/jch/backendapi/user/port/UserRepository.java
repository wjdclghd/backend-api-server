package com.jch.backendapi.user.port;

import com.jch.backendapi.user.domain.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    boolean existsByEmail(String email);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);
}
