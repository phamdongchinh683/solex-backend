package com.example.solex_backend.service;

import com.example.solex_backend.config.ResendEmailConfig;
import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Component;

@Component
public class ResendEmailAdapter implements EmailPort {

    private static final String RESEND_URL = "https://api.resend.com/emails";

    private final RestClient restClient;
    private final ResendEmailConfig resendConfig;

    public ResendEmailAdapter(ResendEmailConfig resendConfig) {
        this.resendConfig = resendConfig;
        this.restClient = RestClient.create();
    }

    @Override
    public void sendOtp(String to, String otp) {
        ResendRequest request = new ResendRequest(
                "Solex <noreply@tiennguyen107.io.vn>",
                to,
                "Your OTP code",
                "<p>Your OTP is: <strong>" + otp + "</strong></p>"
        );

        restClient.post()
                .uri(RESEND_URL)
                .headers(headers -> {
                    headers.setBearerAuth(resendConfig.getApiKey());
                    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                })
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    private record ResendRequest(String from, String to, String subject, String html) {
    }
}