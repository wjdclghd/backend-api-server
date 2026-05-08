package com.jch.backendapi.token.infrastructure;

import com.jch.backendapi.token.domain.AuthToken;
import com.jch.backendapi.token.domain.TokenAuthentication;
import com.jch.backendapi.token.domain.TokenType;
import com.jch.backendapi.token.port.TokenProvider;
import com.jch.backendapi.user.domain.User;
import com.jch.backendapi.user.domain.UserRole;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtTokenProvider implements TokenProvider {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final String issuer;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;
    private final SecureRandom secureRandom;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        SecretKeySpec secretKey = new SecretKeySpec(jwtProperties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.jwtEncoder = new NimbusJwtEncoder(new com.nimbusds.jose.jwk.source.ImmutableSecret<>(secretKey));
        this.jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        this.issuer = jwtProperties.issuer();
        this.accessTokenExpirationSeconds = jwtProperties.accessTokenExpirationSeconds();
        this.refreshTokenExpirationSeconds = jwtProperties.refreshTokenExpirationSeconds();
        this.secureRandom = new SecureRandom();
    }

    @Override
    public AuthToken issueAccessToken(User user) {
        return issueToken(user, TokenType.ACCESS, accessTokenExpirationSeconds);
    }

    @Override
    public AuthToken issueRefreshToken(User user) {
        Instant expiresAt = Instant.now().plusSeconds(refreshTokenExpirationSeconds);
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return new AuthToken(refreshToken, expiresAt);
    }

    @Override
    public Optional<TokenAuthentication> authenticateAccessToken(String accessToken) {
        try {
            Jwt jwt = jwtDecoder.decode(accessToken);
            if (jwt.getIssuer() == null || !issuer.equals(jwt.getIssuer().toString())) {
                return Optional.empty();
            }
            if (!TokenType.ACCESS.name().equals(jwt.getClaimAsString("tokenType"))) {
                return Optional.empty();
            }

            Long userId = Long.valueOf(jwt.getSubject());
            String email = jwt.getClaimAsString("email");
            UserRole role = UserRole.valueOf(jwt.getClaimAsString("role"));
            return Optional.of(new TokenAuthentication(userId, email, role));
        } catch (JwtException | IllegalArgumentException | NullPointerException exception) {
            return Optional.empty();
        }
    }

    private AuthToken issueToken(User user, TokenType tokenType, long expirationSeconds) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(expirationSeconds);
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(String.valueOf(user.id()))
                .claim("email", user.email())
                .claim("role", user.role().name())
                .claim("tokenType", tokenType.name())
                .build();

        String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();
        return new AuthToken(tokenValue, expiresAt);
    }
}
