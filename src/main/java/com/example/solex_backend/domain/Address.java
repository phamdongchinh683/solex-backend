package com.example.solex_backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses", indexes = {
    @Index(name = "idx_address_user_id", columnList = "user_id"),
    @Index(name = "idx_address_phone", columnList = "phone"),
    @Index(name = "idx_address_is_default", columnList = "is_default")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 50)
    private String label;

    @Column(name = "first_name", nullable = false, length = 200)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 200)
    private String lastName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(columnDefinition = "text", nullable = false)
    private String street;

    @Column(length = 100)
    private String ward;

    @Column(length = 100)
    private String district;

    @Column(nullable = false, length = 100)
    private String province;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isDefault == null)
            isDefault = false;
    }
}