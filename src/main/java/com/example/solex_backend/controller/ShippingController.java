package com.example.solex_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.solex_backend.dto.response.ShippingFeeResponse;
import com.example.solex_backend.service.ShippingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/shipping")
@RequiredArgsConstructor
@Tag(name = "Shipping")
@SecurityRequirement(name = "bearerAuth")

public class ShippingController {

    private final ShippingService shippingService;

    @GetMapping("/fee")
    @Operation(summary = "Calculate shipping fee")
    public ResponseEntity<ShippingFeeResponse> calculateFee(
            @RequestParam double restaurantLat,
            @RequestParam double restaurantLng,
            @RequestParam double userLat,
            @RequestParam double userLng) {

        return ResponseEntity.ok(
                shippingService.calculateShippingFee(
                        restaurantLat, restaurantLng, userLat, userLng));
    }
}
