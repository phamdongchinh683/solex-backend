package com.example.solex_backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_restaurant_id", columnList = "restaurant_id"),
    @Index(name = "idx_category_is_active",     columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "text")
    private String imageUrl;

    @Column(name = "is_active")
    private Integer isActive;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) isActive = 1;
    }
}
