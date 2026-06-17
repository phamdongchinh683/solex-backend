package com.example.solex_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResendConfig {

    private final String apiKey;

    public ResendConfig(@Value("${resend.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }
}