package com.example.solex_backend.dto.request;

import com.example.solex_backend.util.Enums;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class VerifyOtpRequest {

    @Schema(description = "Email address or phone number to verify")
    @NotBlank(message = "Value is required")
    private String value;

    @Schema(description = "OTP code (6 digits)")
    @NotBlank(message = "OTP code is required")
    @Size(min = 6, max = 6)
    private String otp;

    @Schema(description = "Field type: EMAIL or PHONE")
    @NotNull(message = "Field is required")
    private Enums.OtpType field;
}
