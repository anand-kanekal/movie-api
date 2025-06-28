package com.movieflix.movieapi.auth.repository;

import com.movieflix.movieapi.auth.entity.ForgotPassword;
import com.movieflix.movieapi.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Integer> {

    @Query("SELECT f FROM ForgotPassword f WHERE f.otp = ?1 AND f.user = ?2")
    Optional<ForgotPassword> findByOtpAndUser(Integer otp, User user);
}
