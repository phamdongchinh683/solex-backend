package com.example.solex_backend.dto.request;

import com.example.solex_backend.util.Enums.OtpType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateContactRequest(
        @Schema(description = "Field to update: EMAIL or PHONE")
        @NotNull(message = "Field is required")
        OtpType field,

        @Schema(description = "New email address or phone number")
        @NotBlank(message = "Value is required")
        String value,

        @Schema(description = "OTP code sent to the new email or phone")
        @NotBlank(message = "OTP is required")
        @Pattern(regexp = "^\\d{6}$", message = "OTP must be exactly 6 digits")
        String otp
) {}
