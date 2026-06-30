package com.example.solex_backend.dto.response;

import com.example.solex_backend.util.Enums;

public record DeviceResponse(
        Long id,
        String fcmToken,
        Enums.DeviceOs deviceOs) {
}
