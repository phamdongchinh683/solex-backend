package com.example.solex_backend.dto.request;

import com.example.solex_backend.util.Enums;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendOtpRequest(
        @Schema(description = "Field type to send OTP: EMAIL or PHONE")
        @NotNull(message = "Field is required")
        Enums.OtpType field,

        @Schema(description = "Email address or phone number to receive OTP")
        @NotBlank(message = "Value is required")
        String value
) {}
