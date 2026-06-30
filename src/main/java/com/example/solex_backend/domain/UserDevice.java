package com.example.solex_backend.domain;

import com.example.solex_backend.util.Enums;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_devices", indexes = {
    @Index(name = "idx_user_device_user_id", columnList = "user_id"),
    @Index(name = "idx_user_device_fcm_token", columnList = "fcm_token")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserDevice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "fcm_token", nullable = false, length = 500, unique = true)
    private String fcmToken;

    @Column(name = "device_os", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Enums.DeviceOs deviceOs;
}
