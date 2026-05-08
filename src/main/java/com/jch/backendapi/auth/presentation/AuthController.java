package com.jch.backendapi.auth.presentation;

import com.jch.backendapi.auth.application.SignupUseCase;
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
    private final EndpointRateLimiter endpointRateLimiter;

    public AuthController(SignupUseCase signupUseCase, EndpointRateLimiter endpointRateLimiter) {
        this.signupUseCase = signupUseCase;
        this.endpointRateLimiter = endpointRateLimiter;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(@Valid @RequestBody SignupRequest request, HttpServletRequest servletRequest) {
        endpointRateLimiter.checkSignup(servletRequest, request.email());
        return signupUseCase.execute(request);
    }
}
