package com.example.solex_backend.service;

import com.example.solex_backend.domain.Category;
import com.example.solex_backend.domain.Product;
import com.example.solex_backend.domain.Restaurant;
import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.request.CreateCategoryRequest;
import com.example.solex_backend.dto.request.UpdateRestaurantRequest;
import com.example.solex_backend.dto.response.CategoryResponse;
import com.example.solex_backend.dto.response.ProductResponse;
import com.example.solex_backend.dto.response.RestaurantDetailResponse;
import com.example.solex_backend.dto.response.RestaurantNearbyResponse;
import com.example.solex_backend.dto.response.RestaurantResponse;
import com.example.solex_backend.dto.response.SliceResponse;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.CategoryRepository;
import com.example.solex_backend.repository.ProductQueryRepository;
import com.example.solex_backend.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantService {

        private final RestaurantRepository restaurantRepository;
        private final CategoryRepository categoryRepository;
        private final ProductQueryRepository productQueryRepository;

        public SliceResponse<RestaurantResponse> getAllRestaurants(String name, Long cursor, int size) {
                List<Restaurant> result = (name != null && !name.isBlank())
                                ? restaurantRepository.searchOpenByNameAfterCursor(name, cursor,
                                                PageRequest.of(0, size + 1))
                                : restaurantRepository.findOpenAfterCursor(cursor, PageRequest.of(0, size + 1));
                boolean hasNext = result.size() > size;
                List<Restaurant> page = hasNext ? result.subList(0, size) : result;
                Long nextCursor = hasNext ? page.get(page.size() - 1).getId() : null;
                return new SliceResponse<>(page.stream().map(this::toRestaurantResponse).toList(), nextCursor);
        }

        public RestaurantDetailResponse getRestaurantById(Long id) {
                Restaurant restaurant = restaurantRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));
                return toRestaurantDetailResponse(restaurant);
        }

        public CategoryResponse createCategory(User operator, CreateCategoryRequest request) {
                Restaurant restaurant = restaurantRepository.findByOperator(operator)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Restaurant not found for this operator"));

                Category category = Category.builder()
                                .restaurant(restaurant)
                                .name(request.name())
                                .imageUrl(request.imageUrl())
                                .isActive(1)
                                .build();

                Category saved = categoryRepository.save(category);
                return new CategoryResponse(saved.getId(), saved.getName(), saved.getImageUrl());
        }

        public SliceResponse<ProductResponse> getMenuByRestaurantId(Long id, Long categoryId, String search,
                        Long cursor, int size) {
                if (!restaurantRepository.existsById(id)) {
                        throw new ResourceNotFoundException("Restaurant not found: " + id);
                }
                List<Product> result = productQueryRepository.findByFilters(id, categoryId, search, cursor, size + 1);
                boolean hasNext = result.size() > size;
                List<Product> page = hasNext ? result.subList(0, size) : result;
                Long nextCursor = hasNext ? page.get(page.size() - 1).getId() : null;
                return new SliceResponse<>(page.stream().map(this::toProductResponse).collect(Collectors.toList()),
                                nextCursor);
        }

        public SliceResponse<RestaurantNearbyResponse> getNearbyRestaurants(
                        double lat, double lng, double radiusKm, int offset, int size) {
                List<Restaurant> result = restaurantRepository.findNearbyOpen(lat, lng, radiusKm, size + 1, offset);
                boolean hasNext = result.size() > size;
                List<Restaurant> page = hasNext ? result.subList(0, size) : result;
                Long nextCursor = hasNext ? (long) (offset + size) : null;
                return new SliceResponse<>(
                                page.stream().map(r -> toRestaurantNearbyResponse(r, lat, lng)).toList(),
                                nextCursor);
        }

        private RestaurantNearbyResponse toRestaurantNearbyResponse(Restaurant r, double userLat, double userLng) {
                double distanceKm = haversine(userLat, userLng, r.getLatitude(), r.getLongitude());
                return new RestaurantNearbyResponse(
                                r.getId(), r.getName(), r.getDescription(), r.getPhone(),
                                r.getAddressDetail(), r.getLongitude(), r.getLatitude(),
                                r.getStar1(), r.getStar2(), r.getStar3(), r.getStar4(), r.getStar5(),
                                r.getIsOpen(), r.getImageUrl(),
                                Math.round(distanceKm * 100.0) / 100.0);
        }

        private double haversine(double lat1, double lng1, double lat2, double lng2) {
                final double R = 6371.0;
                double dLat = Math.toRadians(lat2 - lat1);
                double dLng = Math.toRadians(lng2 - lng1);
                double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                                                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
                return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        }

        private RestaurantResponse toRestaurantResponse(Restaurant r) {
                return new RestaurantResponse(
                                r.getId(), r.getName(), r.getDescription(), r.getPhone(),
                                r.getAddressDetail(), r.getLongitude(), r.getLatitude(),
                                r.getStar1(), r.getStar2(), r.getStar3(), r.getStar4(), r.getStar5(),
                                r.getIsOpen(), r.getImageUrl());
        }

        private RestaurantDetailResponse toRestaurantDetailResponse(Restaurant r) {
                List<CategoryResponse> categories = categoryRepository.findSummariesByRestaurantId(r.getId())
                                .stream()
                                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getImageUrl()))
                                .toList();
                return new RestaurantDetailResponse(
                                r.getId(), r.getName(), r.getDescription(), r.getPhone(),
                                r.getAddressDetail(), r.getLongitude(), r.getLatitude(),
                                r.getStar1(), r.getStar2(), r.getStar3(), r.getStar4(), r.getStar5(),
                                r.getIsOpen(), r.getImageUrl(), categories);
        }

        private ProductResponse toProductResponse(Product p) {
                return new ProductResponse(
                                p.getId(), p.getName(), p.getDescription(), p.getBasePrice(),
                                p.getIsActive(), p.getCategory().getId(), p.getCategory().getName(),
                                p.getImage());
        }

        @Transactional
        public RestaurantDetailResponse updateRestaurant(User operator, UpdateRestaurantRequest request) {
                Restaurant restaurant = restaurantRepository.findByOperator(operator)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Restaurant not found for this operator"));

                if (request.name() != null)
                        restaurant.setName(request.name());
                if (request.description() != null)
                        restaurant.setDescription(request.description());
                if (request.phone() != null)
                        restaurant.setPhone(request.phone());
                if (request.addressDetail() != null)
                        restaurant.setAddressDetail(request.addressDetail());
                if (request.longitude() != null)
                        restaurant.setLongitude(request.longitude());
                if (request.latitude() != null)
                        restaurant.setLatitude(request.latitude());
                if (request.imageUrl() != null)
                        restaurant.setImageUrl(request.imageUrl());

                return toRestaurantDetailResponse(restaurantRepository.save(restaurant));
        }

        @Transactional
        public void updateIsOpenRestaurant(Long id, User operator, boolean status) {
                int rows = restaurantRepository.updateStatusByOperator(id, operator.getId(), status);
                if (rows == 0)
                        throw new ResourceNotFoundException("Not change");
        }
}
