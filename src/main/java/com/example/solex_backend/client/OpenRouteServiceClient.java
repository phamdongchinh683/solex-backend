package com.example.solex_backend.client;

import com.example.solex_backend.config.OpenRouteServiceConfig;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class OpenRouteServiceClient {

    private static final String DIRECTIONS_URL = "https://api.openrouteservice.org/v2/directions/driving-car";
    private static final Logger log = LoggerFactory.getLogger(OpenRouteServiceClient.class);
    private final String apiKey;
    private final RestClient restClient;

    public record RouteDetails(double distanceKm, double durationSeconds, List<List<Double>> coordinates) {
    }

    public OpenRouteServiceClient(OpenRouteServiceConfig config) {
        this.apiKey = config.getApiKey();
        this.restClient = RestClient.builder()
                .baseUrl(DIRECTIONS_URL)
                .requestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory(
                        java.net.http.HttpClient.newBuilder()
                                .connectTimeout(Duration.ofSeconds(10))
                                .build()))
                .build();
    }

    public RouteDetails getRouteDetails(double startLng, double startLat, double endLng, double endLat) {
        String body = """
                {
                  "coordinates": [
                    [%s, %s],
                    [%s, %s]
                  ]
                }
                """.formatted(startLng, startLat, endLng, endLat);

        try {
            JsonNode root = restClient.post()
                    .uri("")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Accept", "application/json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        log.error("ORS error: status={}", res.getStatusCode());
                        throw new IllegalStateException("ORS error: " + res.getStatusCode());
                    })
                    .body(JsonNode.class);

            if (root == null || !root.has("routes") || root.get("routes").isEmpty()) {
                throw new IllegalStateException("ORS: invalid response - missing routes");
            }

            JsonNode route = root.get("routes").get(0);
            JsonNode summary = route.get("summary");
            double distanceKm = summary.get("distance").asDouble() / 1000.0;
            double durationSeconds = summary.get("duration").asDouble();

            List<List<Double>> coordinates = decodePolyline(route.get("geometry").asText());

            return new RouteDetails(distanceKm, durationSeconds, coordinates);

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("ORS request failed: {}", e.getMessage(), e);
            throw new IllegalStateException("ORS request failed: " + e.getMessage(), e);
        }
    }

    private List<List<Double>> decodePolyline(String encoded) {
        List<List<Double>> result = new ArrayList<>();
        int index = 0;
        int lat = 0, lng = 0;

        while (index < encoded.length()) {
            int shift = 0, value = 0, b;
            do {
                b = encoded.charAt(index++) - 63;
                value |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            lat += ((value & 1) != 0 ? ~(value >> 1) : (value >> 1));

            shift = 0;
            value = 0;
            do {
                b = encoded.charAt(index++) - 63;
                value |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            lng += ((value & 1) != 0 ? ~(value >> 1) : (value >> 1));

            result.add(List.of(lng / 1e5, lat / 1e5));
        }

        return result;
    }
}
