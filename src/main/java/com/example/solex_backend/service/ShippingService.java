package com.example.solex_backend.service;

import com.example.solex_backend.client.OpenRouteServiceClient;
import com.example.solex_backend.dto.response.ShippingFeeResponse;
import org.springframework.stereotype.Service;

@Service
public class ShippingService {
    private static final double BASE_FEE = 15_000;
    private static final double RATE_PER_KM = 5_000;

    private final OpenRouteServiceClient openRouteServiceClient;

    public ShippingService(OpenRouteServiceClient openRouteServiceClient) {
        this.openRouteServiceClient = openRouteServiceClient;
    }

    public ShippingFeeResponse calculateShippingFee(
            double resraurantLong, double restaurantLat,
            double userLong, double userLat) {

        double distanceKm = openRouteServiceClient.getRouteDistanceKm(
                resraurantLong, restaurantLat, userLong, userLat);

        double fee = BASE_FEE + (distanceKm * RATE_PER_KM);

        return new ShippingFeeResponse(distanceKm, fee);
    }
}
