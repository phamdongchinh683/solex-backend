package com.example.solex_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @Schema(description = "User email address")
    private String email;

    @Schema(description = "Phone number (10-14 digits)")
    private String phone;

    @Schema(description = "Account password")
    @NotBlank(message = "Password cannot be empty")
    private String password;
}
