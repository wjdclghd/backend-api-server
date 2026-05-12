package com.jch.backendapi.token.port;

public interface RefreshTokenHasher {

    String hash(String refreshToken);
}
