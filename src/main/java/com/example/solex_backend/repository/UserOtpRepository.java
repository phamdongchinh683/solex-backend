package com.example.solex_backend.repository;

import com.example.solex_backend.domain.UserOtp;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserOtpRepository extends JpaRepository<UserOtp, Long> {

    Optional<UserOtp> findByEmail(String email);

    Optional<UserOtp> findByPhone(String phone);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO user_otps (email, phone, otp, expires_at)
        VALUES (:email, :phone, :otp, :expiresAt)
        ON CONFLICT (email) DO UPDATE SET
            otp = EXCLUDED.otp,
            expires_at = EXCLUDED.expires_at
        """, nativeQuery = true)
    void upsertOtpByEmail(
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("otp") String otp,
            @Param("expiresAt") LocalDateTime expiresAt
    );

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO user_otps (email, phone, otp, expires_at)
        VALUES (:email, :phone, :otp, :expiresAt)
        ON CONFLICT (phone) DO UPDATE SET
            otp = EXCLUDED.otp,
            expires_at = EXCLUDED.expires_at
        """, nativeQuery = true)
    void upsertOtpByPhone(
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("otp") String otp,
            @Param("expiresAt") LocalDateTime expiresAt
    );
}
