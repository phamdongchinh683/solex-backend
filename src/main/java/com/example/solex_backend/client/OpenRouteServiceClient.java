package com.example.solex_backend.client;

import com.example.solex_backend.config.OpenRouteServiceConfig;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenRouteServiceClient {

    private static final String DIRECTIONS_URL = "https://api.openrouteservice.org/v2/directions/driving-car";
    private static final Logger log = LoggerFactory.getLogger(OpenRouteServiceClient.class);
    private final String apiKey;
    private final RestClient restClient;

    public OpenRouteServiceClient(OpenRouteServiceConfig config) {
        this.apiKey = config.getApiKey();
        this.restClient = RestClient.create();

    }

    public double getRouteDistanceKm(double startLng, double startLat, double endLng, double endLat) {
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
                    .uri(DIRECTIONS_URL)
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

            JsonNode summary = root.get("routes").get(0).get("summary");
            if (summary == null || !summary.has("distance")) {
                throw new IllegalStateException("ORS: missing distance in summary");
            }

            return summary.get("distance").asDouble() / 1000.0;

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("ORS request failed: {}", e.getMessage(), e);
            throw new IllegalStateException("ORS request failed: " + e.getMessage(), e);
        }
    }

}