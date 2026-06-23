package com.example.solex_backend.controller;

import com.example.solex_backend.dto.ApiResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Health", description = "Health check endpoint")
public class HealthController {

    @Operation(summary = "Check server health status")
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("Máy chủ đang hoạt động", "Máy chủ đang hoạt động");
    }
}