package com.example.solex_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "ratings", indexes = {
        @Index(name = "idx_rating_restaurant_id", columnList = "restaurant_id"),
        @Index(name = "idx_rating_user_id",       columnList = "user_id"),
        @Index(name = "idx_rating_rating",         columnList = "rating"),
        @Index(name = "idx_rating_created_at",     columnList = "created_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_ratings_order", columnNames = {"order_id"})
})
@Check(constraints = "rating between 1 and 5")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "text")
    private String comment;
}
