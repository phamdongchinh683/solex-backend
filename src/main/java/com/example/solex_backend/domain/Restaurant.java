package com.example.solex_backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurants", indexes = {
        @Index(name = "idx_restaurant_is_open", columnList = "is_open"),
        @Index(name = "idx_restaurant_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", unique = true)
    private User operator;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 14)
    private String phone;

    @Column(length = 100)
    private String addressDetail;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private double latitude;

    @Column(name = "star_1", nullable = false, columnDefinition = "integer default 0")
    private Integer star1;

    @Column(name = "star_2", nullable = false, columnDefinition = "integer default 0")
    private Integer star2;

    @Column(name = "star_3", nullable = false, columnDefinition = "integer default 0")
    private Integer star3;

    @Column(name = "star_4", nullable = false, columnDefinition = "integer default 0")
    private Integer star4;

    @Column(name = "star_5", nullable = false, columnDefinition = "integer default 0")
    private Integer star5;

    @Column(name = "is_open")
    private Boolean isOpen;

    @Column(columnDefinition = "text")
    private String imageUrl;

    @Column(name = "stripe_account_id", length = 100)
    private String stripeAccountId;
}
