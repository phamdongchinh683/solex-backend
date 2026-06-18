package com.example.solex_backend.controller;

import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.ApiResponse;
import com.example.solex_backend.dto.request.CreateRatingRequest;
import com.example.solex_backend.dto.response.RatingResponse;
import com.example.solex_backend.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Ratings", description = "Restaurant rating management")
@RestController
@RequestMapping("/api/v1/restaurants/{id}/ratings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RatingController {

    private final RatingService ratingService;

    @Operation(summary = "Create or update my restaurant rating")
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<RatingResponse> rateRestaurant(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestBody @Valid CreateRatingRequest request) {
        return ApiResponse.ok("OK", ratingService.rateRestaurant(
                id,
                user,
                request
        ));
    }

    @Operation(summary = "Get restaurant ratings")
    @GetMapping
    public ApiResponse<List<RatingResponse>> getRestaurantRatings(@PathVariable Long id) {
        return ApiResponse.ok("OK", ratingService.getRestaurantRatings(id));
    }
}
