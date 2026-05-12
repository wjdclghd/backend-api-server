package com.jch.backendapi.global.security;

import com.jch.backendapi.global.error.ErrorCode;
import com.jch.backendapi.token.domain.TokenAuthentication;
import com.jch.backendapi.token.port.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_PREFIX = "ROLE_";

    private final TokenProvider tokenProvider;

    public JwtAuthenticationFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(authorizationHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            writeUnauthorizedResponse(response);
            return;
        }

        String accessToken = authorizationHeader.substring(BEARER_PREFIX.length());
        Optional<TokenAuthentication> tokenAuthentication = tokenProvider.authenticateAccessToken(accessToken);
        if (tokenAuthentication.isEmpty()) {
            writeUnauthorizedResponse(response);
            return;
        }

        setAuthentication(request, tokenAuthentication.get());
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(HttpServletRequest request, TokenAuthentication tokenAuthentication) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(ROLE_PREFIX + tokenAuthentication.role().name());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                tokenAuthentication,
                null,
                List.of(authority)
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void writeUnauthorizedResponse(HttpServletResponse response) throws IOException {
        ErrorCode errorCode = ErrorCode.INVALID_ACCESS_TOKEN;
        response.setStatus(errorCode.httpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {"code":"%s","message":"%s","details":[],"timestamp":"%s"}""".formatted(
                errorCode.code(),
                errorCode.message(),
                Instant.now()
        ));
    }
}
