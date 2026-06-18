package com.example.solex_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @Schema(description = "User email address")
        @Email(message = "Email is invalid")
        String email,

        @Schema(description = "Phone number (10-14 digits)")
        @Pattern(regexp = "^\\d{10,14}$", message = "Phone number must be 10-14 digits")
        String phone,

        @Schema(description = "Account password")
        @NotBlank(message = "Password cannot be empty")
        String password
) {}
