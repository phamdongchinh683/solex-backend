package com.example.solex_backend.service;

import org.springframework.stereotype.Service;

import com.example.solex_backend.dto.response.ShippingFeeResponse;

@Service
public class ShippingService {
    private static final double BASE_FEE = 15_000;
    private static final double RATE_PER_KM = 5_000;
    private static final double MAX_FEE = 50_000;
    private static final double ROAD_FACTOR = 1.3;
    private static final double EARTH_RADIUS_KM = 6371.0;

    public ShippingFeeResponse calculateShippingFee(
            double restaurantLat, double restaurantLng,
            double userLat, double userLng) {

        double straightKm = haversine(restaurantLat, restaurantLng, userLat, userLng);
        double estimatedKm = straightKm * ROAD_FACTOR;
        double fee = Math.min(BASE_FEE + (estimatedKm * RATE_PER_KM), MAX_FEE);

        return new ShippingFeeResponse(estimatedKm, fee);
    }

    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
