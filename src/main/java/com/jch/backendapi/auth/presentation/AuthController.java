package com.jch.backendapi.auth.presentation;

import com.jch.backendapi.auth.application.LoginUseCase;
import com.jch.backendapi.auth.application.LogoutUseCase;
import com.jch.backendapi.auth.application.RefreshTokenUseCase;
import com.jch.backendapi.auth.application.SignupUseCase;
import com.jch.backendapi.auth.dto.LoginRequest;
import com.jch.backendapi.auth.dto.LoginResponse;
import com.jch.backendapi.auth.dto.RefreshTokenRequest;
import com.jch.backendapi.auth.dto.SignupRequest;
import com.jch.backendapi.auth.dto.SignupResponse;
import com.jch.backendapi.global.security.EndpointRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SignupUseCase signupUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final EndpointRateLimiter endpointRateLimiter;

    public AuthController(
            SignupUseCase signupUseCase,
            LoginUseCase loginUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            LogoutUseCase logoutUseCase,
            EndpointRateLimiter endpointRateLimiter
    ) {
        this.signupUseCase = signupUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.endpointRateLimiter = endpointRateLimiter;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(@Valid @RequestBody SignupRequest request, HttpServletRequest servletRequest) {
        endpointRateLimiter.checkSignup(servletRequest, request.email());
        return signupUseCase.execute(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        endpointRateLimiter.checkLogin(servletRequest, request.email());
        return loginUseCase.execute(request);
    }

    @PostMapping("/refresh")
    public LoginResponse refreshToken(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest servletRequest) {
        endpointRateLimiter.checkRefresh(servletRequest, request.refreshToken());
        return refreshTokenUseCase.execute(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest servletRequest) {
        endpointRateLimiter.checkLogout(servletRequest, request.refreshToken());
        logoutUseCase.execute(request);
    }
}
