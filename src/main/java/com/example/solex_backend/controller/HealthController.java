package com.example.solex_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@Tag(name ="health")
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("message", "OK");
    }
}