package com.jch.backendapi.user.port;

import com.jch.backendapi.user.domain.User;

public interface UserRepository {

    User save(User user);

    boolean existsByEmail(String email);
}
