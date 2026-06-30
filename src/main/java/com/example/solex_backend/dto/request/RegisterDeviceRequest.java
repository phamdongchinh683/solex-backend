package com.example.solex_backend.dto.request;

import com.example.solex_backend.util.Enums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterDeviceRequest(
        @NotBlank(message = "FCM token không được để trống")
        String token,

        @NotNull(message = "Hệ điều hành không được để trống")
        Enums.DeviceOs deviceOs
) {}
