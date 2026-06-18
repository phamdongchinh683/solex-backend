package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(
        @Schema(description = "JWT access token")            String token,
        @Schema(description = "Authenticated user information") UserInfoResponse user
) {}
