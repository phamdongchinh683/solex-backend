package com.example.solex_backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_otps", indexes = {
    @Index(name = "idx_otp_email", columnList = "email"),
    @Index(name = "idx_otp_phone", columnList = "phone"),
    @Index(name = "idx_otp_code", columnList = "otp"),
    @Index(name = "idx_otp_expires_at", columnList = "expires_at")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uc_otp_email", columnNames = "email"),
    @UniqueConstraint(name = "uc_otp_phone", columnNames = "phone")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 14, unique = true)
    private String phone;

    @Column(length = 255, unique = true)
    private String email;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "verified")
    private Boolean verified;

    @Column(name = "otp")
    private String otp;
}
