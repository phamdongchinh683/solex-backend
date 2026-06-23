package com.example.solex_backend.service;

import com.example.solex_backend.domain.Order;
import com.example.solex_backend.domain.Rating;
import com.example.solex_backend.domain.Restaurant;
import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.request.CreateRatingRequest;
import com.example.solex_backend.dto.response.RatingResponse;
import com.example.solex_backend.dto.response.SliceResponse;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.OrderRepository;
import com.example.solex_backend.repository.RatingRepository;
import com.example.solex_backend.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
            throw new BusinessException("Đơn hàng phải ở trạng thái ĐÃ GIAO trước khi đánh giá");
        }
        if (Boolean.TRUE.equals(order.getRate())) {
            throw new BusinessException("Đơn hàng này đã được đánh giá");
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
    public SliceResponse<RatingResponse> getRestaurantRatings(Long restaurantId, Integer star, Long cursor, int size) {
        if (star != null && (star < 1 || star > 5)) {
            throw new BusinessException("Bộ lọc sao phải từ 1 đến 5");
        }
        List<Rating> result = star != null
                ? ratingRepository.findByRestaurantAndStarBeforeCursor(restaurantId, star, cursor, PageRequest.of(0, size + 1))
                : ratingRepository.findByRestaurantBeforeCursor(restaurantId, cursor, PageRequest.of(0, size + 1));
        boolean hasNext = result.size() > size;
        List<Rating> page = hasNext ? result.subList(0, size) : result;
        Long nextCursor = hasNext ? page.get(page.size() - 1).getId() : null;
        return new SliceResponse<>(page.stream().map(this::toResponse).toList(), nextCursor);
    }

    private int validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException("Đánh giá phải từ 1 đến 5");
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
            default -> throw new BusinessException("Đánh giá phải từ 1 đến 5");
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
                rating.getCreatedAt());
    }
}
