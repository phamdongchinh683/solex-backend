package com.example.solex_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

    private final String serviceAccountPath;
    private final ResourceLoader resourceLoader;

    public FirebaseConfig(
            @Value("${firebase.service-account-path:}") String serviceAccountPath,
            ResourceLoader resourceLoader) {
        this.serviceAccountPath = serviceAccountPath;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void initialize() {
        if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
            log.warn("Firebase not configured — push notifications disabled");
            return;
        }
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }
        try {
            Resource resource = resourceLoader.getResource(serviceAccountPath);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully");
        } catch (IOException e) {
            log.warn("Firebase initialization failed — push notifications disabled: {}", e.getMessage());
        }
    }
}
