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
            double restaurantLng, double restaurantLat,
            double userLng, double userLat) {

        OpenRouteServiceClient.RouteDetails route = openRouteServiceClient.getRouteDetails(
                restaurantLng, restaurantLat, userLng, userLat);

        double fee = BASE_FEE + (route.distanceKm() * RATE_PER_KM);

        return new ShippingFeeResponse(route.distanceKm(), fee, route.durationSeconds(), route.coordinates());
    }
}
