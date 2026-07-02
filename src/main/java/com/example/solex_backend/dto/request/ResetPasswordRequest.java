package com.example.solex_backend.dto.request;

import com.example.solex_backend.util.Enums;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @Schema(description = "Email address or phone number") @NotBlank(message = "Please enter email or phone number") String value,

        @Schema(description = "OTP code (6 digits)") @NotBlank(message = "Please enter OTP code") @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits") String otp,

        @Schema(description = "Field type: EMAIL or PHONE") @NotNull(message = "Please select field type") Enums.OtpType field,

        @Schema(description = "New password") @NotBlank(message = "Please enter new password") @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters") String password) {
}