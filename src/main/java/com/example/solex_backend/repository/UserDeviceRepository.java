package com.example.solex_backend.repository;

import com.example.solex_backend.domain.User;
import com.example.solex_backend.domain.UserDevice;
import com.example.solex_backend.util.Enums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    Optional<UserDevice> findByUserAndFcmToken(User user, String fcmToken);
    List<UserDevice> findByUserOrderByCreatedAtDesc(User user);

    @Modifying
    @Query(value = """
            INSERT INTO user_devices (user_id, fcm_token, device_os, created_at, updated_at)
            VALUES (:userId, :fcmToken, :deviceOs, NOW(), NOW())
            ON CONFLICT (fcm_token) DO UPDATE SET
                user_id = :userId,
                device_os = :deviceOs,
                updated_at = NOW()
            """, nativeQuery = true)
    void upsertDevice(@Param("userId") Long userId,
                      @Param("fcmToken") String fcmToken,
                      @Param("deviceOs") String deviceOs);
}