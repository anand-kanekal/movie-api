package com.movieflix.movieapi.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ForgotPassword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer forgotPasswordId;

    @Column(nullable = false)
    private Integer otp;

    @Column(nullable = false)
    private Date expiryTime;

    @OneToOne
    private User user;
}
