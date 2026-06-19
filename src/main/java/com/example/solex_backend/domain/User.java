package com.example.solex_backend.domain;

import jakarta.persistence.*;
import lombok.*;

import com.example.solex_backend.util.Enums;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_phone", columnList = "phone"),
    @Index(name = "idx_user_role", columnList = "role"),
    @Index(name = "idx_user_is_active", columnList = "is_active")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(length = 14)
    private String phone;

    @Column(name = "token_version")
    private int tokenVersion;

    @Column(nullable = false, length = 20)
    private Enums.UserRole role;

    @Column(name = "is_email_verified")
    private Integer isEmailVerified;

    @Column(name = "is_phone_verified")
    private Integer isPhoneVerified;

    @Column(name = "is_active")
    private Integer isActive;

    @Column(name = "stripe_customer_id", length = 100)
    private String stripeCustomerId;
}
