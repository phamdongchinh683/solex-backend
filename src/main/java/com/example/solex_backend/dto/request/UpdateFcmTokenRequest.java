package com.example.solex_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateFcmTokenRequest(
        @NotBlank(message = "FCM token không được để trống")
        String token
) {}
