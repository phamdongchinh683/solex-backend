package com.example.solex_backend.dto.request;

import com.example.solex_backend.util.Enums;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContactCheckRequest(
        @Schema(description = "Field type to check: EMAIL or PHONE")
        @NotNull(message = "Field is required")
        Enums.OtpType field,

        @Schema(description = "Email address or phone number to check")
        @NotBlank(message = "Value is required")
        String value
) {}
