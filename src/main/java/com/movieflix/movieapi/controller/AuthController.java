package com.movieflix.movieapi.controller;

import com.movieflix.movieapi.auth.entity.RefreshToken;
import com.movieflix.movieapi.auth.entity.User;
import com.movieflix.movieapi.auth.service.AuthService;
import com.movieflix.movieapi.auth.service.JwtService;
import com.movieflix.movieapi.auth.service.RefreshTokenService;
import com.movieflix.movieapi.auth.util.AuthResponse;
import com.movieflix.movieapi.auth.util.LoginRequest;
import com.movieflix.movieapi.auth.util.RefreshTokenRequest;
import com.movieflix.movieapi.auth.util.RegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/")
public class AuthController {

    private final AuthService authService;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenRequest.getRefreshToken());
        User user = refreshToken.getUser();
        var accessToken = jwtService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .build());
    }
}
