package com.example.solex_backend.dto.response;

import com.example.solex_backend.util.Enums;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record UserInfoResponse(
        @Schema(description = "User ID")                    Long id,
        @Schema(description = "User email address")         String email,
        @Schema(description = "First name")                 String firstName,
        @Schema(description = "Last name")                  String lastName,
        @Schema(description = "Phone number")               String phone,
        @Schema(description = "User role")                  Enums.UserRole role,
        @Schema(description = "Email verified flag")        Integer isEmailVerified,
        @Schema(description = "Phone verified flag")        Integer isPhoneVerified,
        @Schema(description = "Account active flag")        Integer isActive,
        @Schema(description = "Account creation timestamp") LocalDateTime createdAt
) {}
