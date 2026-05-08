package com.jch.backendapi.token.port;

import com.jch.backendapi.token.domain.AuthToken;
import com.jch.backendapi.user.domain.User;

public interface TokenProvider {

    AuthToken issueAccessToken(User user);

    AuthToken issueRefreshToken(User user);
}
