package com.movieflix.movieapi.auth.service;

import com.movieflix.movieapi.auth.entity.RefreshToken;
import com.movieflix.movieapi.auth.entity.User;
import com.movieflix.movieapi.auth.repository.RefreshTokenRepository;
import com.movieflix.movieapi.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final UserRepository userRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken generateRefreshToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + email));

        RefreshToken refreshToken = user.getRefreshToken();

        if (refreshToken == null) {
            refreshToken = RefreshToken.builder()
                    .refreshToken(UUID.randomUUID().toString())
                    .expirationTime(Instant.now().plusMillis(30 * 1000))
                    .user(user)
                    .build();

            refreshTokenRepository.save(refreshToken);
        }

        return refreshToken;
    }

    public RefreshToken verifyRefreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByRefreshToken(refreshToken).orElseThrow(
                () -> new RuntimeException("Invalid refresh token"));

        if (token.getExpirationTime().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired");
        }

        return token;
    }
}
