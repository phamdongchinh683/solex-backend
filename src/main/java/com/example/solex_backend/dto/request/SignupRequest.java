package com.example.solex_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
                @Schema(description = "User email address") @NotBlank(message = "Email must not be empty") @Email(message = "Email is invalid") String email,
                @Schema(description = "Account password (min 6 characters)") @NotBlank(message = "Password must not be empty") @Size(min = 6, message = "Password must be at least 6 characters") String password,

                @Schema(description = "First name") @NotBlank(message = "First name must not be empty") String firstName,

                @Schema(description = "Last name") @NotBlank(message = "Last name must not be empty") String lastName,

                @Schema(description = "Phone number (optional)") String phone) {

}
