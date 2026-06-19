package com.example.solex_backend.service;

import com.example.solex_backend.domain.Order;
import com.example.solex_backend.domain.Rating;
import com.example.solex_backend.domain.Restaurant;
import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.request.CreateRatingRequest;
import com.example.solex_backend.dto.response.RatingResponse;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.OrderRepository;
import com.example.solex_backend.repository.RatingRepository;
import com.example.solex_backend.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RatingService {

    private final RatingRepository ratingRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;

    public RatingResponse rateRestaurant(Long restaurantId, User user, CreateRatingRequest request) {
        Order order = orderRepository.findByIdAndUser(request.orderId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.orderId()));

        if (!"DELIVERED".equals(order.getStatus())) {
            throw new BusinessException("Order must be DELIVERED before rating");
        }
        if (Boolean.TRUE.equals(order.getRate())) {
            throw new BusinessException("This order has already been rated");
        }

        int newRating = validateRating(request.rating());
        Restaurant restaurant = restaurantRepository.findByIdForUpdate(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));

        Rating rating = ratingRepository.findByRestaurantAndUser(restaurant, user)
                .orElse(null);

        if (rating == null) {
            rating = Rating.builder()
                    .restaurant(restaurant)
                    .user(user)
                    .rating(newRating)
                    .comment(normalizeComment(request.comment()))
                    .build();
            updateStarCounter(restaurant, newRating, 1);
        } else {
            int oldRating = rating.getRating();
            if (oldRating != newRating) {
                updateStarCounter(restaurant, oldRating, -1);
                updateStarCounter(restaurant, newRating, 1);
            }
            rating.setRating(newRating);
            rating.setComment(normalizeComment(request.comment()));
        }

        restaurantRepository.save(restaurant);
        Rating savedRating = ratingRepository.save(rating);
        orderRepository.markRated(order.getId());
        return toResponse(savedRating);
    }

    @Transactional(readOnly = true)
    public List<RatingResponse> getRestaurantRatings(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));

        return ratingRepository.findByRestaurantOrderByCreatedAtDesc(restaurant).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private int validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException("Rating must be between 1 and 5");
        }
        return rating;
    }

    private String normalizeComment(String comment) {
        if (comment == null || comment.isBlank()) {
            return null;
        }
        return comment.trim();
    }

    private void updateStarCounter(Restaurant restaurant, int rating, int delta) {
        switch (rating) {
            case 1 -> restaurant.setStar1(nextCounterValue(restaurant.getStar1(), delta));
            case 2 -> restaurant.setStar2(nextCounterValue(restaurant.getStar2(), delta));
            case 3 -> restaurant.setStar3(nextCounterValue(restaurant.getStar3(), delta));
            case 4 -> restaurant.setStar4(nextCounterValue(restaurant.getStar4(), delta));
            case 5 -> restaurant.setStar5(nextCounterValue(restaurant.getStar5(), delta));
            default -> throw new BusinessException("Rating must be between 1 and 5");
        }
    }

    private int nextCounterValue(Integer currentValue, int delta) {
        int current = currentValue == null ? 0 : currentValue;
        return Math.max(0, current + delta);
    }

    private RatingResponse toResponse(Rating rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getRestaurant().getId(),
                rating.getUser().getId(),
                rating.getRating(),
                rating.getComment(),
                rating.getCreatedAt(),
                rating.getUpdatedAt()
        );
    }
}
