package com.example.solex_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResendEmailConfig {

    private final String apiKey;

    public ResendEmailConfig(@Value("${resend.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }
}