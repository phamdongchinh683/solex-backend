package com.example.solex_backend.dto.request;

import com.example.solex_backend.util.Enums;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(
        @Schema(description = "Email address or phone number to verify")
        @NotBlank(message = "Value is required")
        String value,

        @Schema(description = "OTP code (6 digits)")
        @NotBlank(message = "OTP code is required")
        @Pattern(regexp = "^\\d{6}$", message = "OTP must be exactly 6 digits")
        String otp,

        @Schema(description = "Field type: EMAIL or PHONE")
        @NotNull(message = "Field is required")
        Enums.OtpType field
) {}
