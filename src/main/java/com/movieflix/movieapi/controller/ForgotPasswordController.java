package com.movieflix.movieapi.controller;

import com.movieflix.movieapi.auth.entity.ForgotPassword;
import com.movieflix.movieapi.auth.entity.User;
import com.movieflix.movieapi.auth.repository.ForgotPasswordRepository;
import com.movieflix.movieapi.auth.repository.UserRepository;
import com.movieflix.movieapi.auth.util.ChangePassword;
import com.movieflix.movieapi.dto.MailBody;
import com.movieflix.movieapi.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.Random;

@RestController
@RequestMapping("/forgot-password")
public class ForgotPasswordController {

    private UserRepository userRepository;

    private EmailService emailService;

    private ForgotPasswordRepository forgotPasswordRepository;

    private PasswordEncoder passwordEncoder;

    public ForgotPasswordController(UserRepository userRepository, EmailService emailService, ForgotPasswordRepository forgotPasswordRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.forgotPasswordRepository = forgotPasswordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/verify-email/{email}")
    public ResponseEntity<String> verifyEmail(@PathVariable String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + email));

        Integer otp = generateOtp();

        MailBody mailBody = MailBody.builder()
                .to(email)
                .subject("Forgot Password")
                .text("This is your OTP for forgot password: " + otp)
                .build();

        ForgotPassword forgotPassword = ForgotPassword.builder()
                .otp(otp)
                .expiryTime(new Date(System.currentTimeMillis() + 70 * 1000))
                .user(user)
                .build();

        emailService.sendSimpleMessage(mailBody);
        forgotPasswordRepository.save(forgotPassword);

        return ResponseEntity.ok("OTP sent successfully");
    }

    @PostMapping("/verify-otp/{otp}/{email}")
    public ResponseEntity<String> verifyOtp(@PathVariable Integer otp, @PathVariable String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + email));

        ForgotPassword forgotPassword = forgotPasswordRepository.findByOtpAndUser(otp, user)
                .orElseThrow(() -> new RuntimeException("Invalid OTP for email: " + email));

        if (forgotPassword.getExpiryTime().before(Date.from(Instant.now()))) {
            forgotPasswordRepository.deleteById(forgotPassword.getForgotPasswordId());
            return new ResponseEntity<>("OTP expired", HttpStatus.EXPECTATION_FAILED);
        }

        return ResponseEntity.ok("OTP verified successfully");
    }

    @PostMapping("/change-password/{email}")
    public ResponseEntity<String> changePassword(@RequestBody ChangePassword changePassword, @PathVariable String email) {
        if (!changePassword.password().equals(changePassword.confirmPassword())) {
            return new ResponseEntity<>("Password and confirm password do not match", HttpStatus.BAD_REQUEST);
        }

        String encodedPassword = passwordEncoder.encode(changePassword.password());
        userRepository.updatePassword(email, encodedPassword);
        return ResponseEntity.ok("Password changed successfully");
    }

    private Integer generateOtp() {
        Random random = new Random();
        return random.nextInt(100_000, 999_999);
    }
}
