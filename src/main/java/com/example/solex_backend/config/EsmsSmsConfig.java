package com.example.solex_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EsmsSmsConfig {

    private final String apiKey;
    private final String secret;

    public EsmsSmsConfig(
            @Value("${esms.api-key}") String apiKey,
            @Value("${esms.secret}") String secret) {
        this.apiKey = apiKey;
        this.secret = secret;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSecret() {
        return secret;
    }
}