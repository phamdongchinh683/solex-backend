package com.example.solex_backend.dto.request;

import com.example.solex_backend.util.Enums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterDeviceRequest(
        @NotBlank(message = "FCM token must not be empty")
        String token,

        @NotNull(message = "Operating system must not be empty")
        Enums.DeviceOs deviceOs
) {}
