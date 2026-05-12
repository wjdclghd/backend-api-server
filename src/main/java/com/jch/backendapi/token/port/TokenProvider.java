package com.jch.backendapi.token.port;

import com.jch.backendapi.token.domain.AuthToken;
import com.jch.backendapi.token.domain.TokenAuthentication;
import com.jch.backendapi.user.domain.User;

import java.util.Optional;

public interface TokenProvider {

    AuthToken issueAccessToken(User user);

    AuthToken issueRefreshToken(User user);

    Optional<TokenAuthentication> authenticateAccessToken(String accessToken);
}
